"""Partition Maven tests and transfer their reports and JaCoCo data between CI jobs."""

import argparse
import io
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import tarfile
import xml.etree.ElementTree as ET


ROOT = Path.cwd()
SHARDS = ("engine", "spring", "other")
REPORT_DIRECTORIES = ("surefire-reports", "failsafe-reports")
NAMESPACE = {"m": "http://maven.apache.org/POM/4.0.0"}
MANIFEST_FILENAME = "manifest.json"
REPORTS = "reports"
COVERAGE = "coverage"


def module_shard(directory):
    project = ET.parse(directory / "pom.xml").getroot()
    artifact_id = project.findtext("m:artifactId", namespaces=NAMESPACE)
    if artifact_id == "activiti-engine":
        return "engine"
    if artifact_id.startswith("activiti-spring"):
        return "spring"
    return "other"


def leaf_modules(directory):
    project = ET.parse(directory / "pom.xml").getroot()
    children = project.findall("m:modules/m:module", NAMESPACE)
    if not children:
        yield directory
    for child in children:
        yield from leaf_modules(directory / child.text)


def projects(shard):
    selected = [
        directory.relative_to(ROOT).as_posix()
        for directory in leaf_modules(ROOT)
        if module_shard(directory) == shard
    ]
    if not selected:
        raise ValueError(f"No Maven modules found for {shard}")
    return ",".join(selected)


def summarize(reports):
    counts = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
    for report in reports:
        suite = ET.parse(report).getroot()
        for key in counts:
            counts[key] += int(suite.get(key, "0"))
    return counts


def archive_name(shard):
    if shard not in SHARDS:
        raise ValueError(f"Unknown test shard: {shard}")
    return f"test-results-{shard}.tar.gz"


def collect(shard):
    output = ROOT / "target/ci-test-artifacts" / archive_name(shard)
    reports = sorted(
        report
        for directory in REPORT_DIRECTORIES
        for report in ROOT.glob(f"**/target/{directory}/TEST-*.xml")
    )
    for report in reports:
        if module_shard(report.parents[2]) != shard:
            raise ValueError(f"Test report belongs to another shard: {report}")
    coverage = sorted(ROOT.glob("**/target/*.exec"))
    counts = summarize(reports)
    if not counts["tests"] or not coverage:
        raise ValueError(f"No tests or coverage were produced for {shard}")
    manifest = {
        "shard": shard,
        "counts": counts,
        REPORTS: len(reports),
        "coverage_files": len(coverage),
    }
    data = json.dumps(manifest).encode()
    output.parent.mkdir(parents=True, exist_ok=True)
    with tarfile.open(output, "w:gz") as archive:
        header = tarfile.TarInfo(MANIFEST_FILENAME)
        header.size = len(data)
        archive.addfile(header, io.BytesIO(data))
        for prefix, files in ((REPORTS, reports), (COVERAGE, coverage)):
            for path in files:
                archive.add(path, arcname=f"{prefix}/{path.relative_to(ROOT).as_posix()}")
    print(json.dumps(manifest))


def result_destination(shard, member):
    path = PurePosixPath(member.name)
    if (
        not member.isfile()
        or path.is_absolute()
        or ".." in path.parts
        or len(path.parts) < 2
    ):
        raise ValueError(f"Invalid test result path: {member.name}")
    relative = Path(*path.parts[1:])
    if path.parts[0] == COVERAGE and relative.suffix == ".exec":
        return COVERAGE, ROOT / "target/ci-coverage" / shard / relative
    if (
        path.parts[0] != REPORTS
        or relative.parent.name not in REPORT_DIRECTORIES
        or not relative.name.startswith("TEST-")
        or relative.suffix != ".xml"
    ):
        raise ValueError(f"Unexpected test result: {member.name}")
    if module_shard((ROOT / relative).parents[2]) != shard:
        raise ValueError(f"Test report is in the wrong shard: {relative}")
    return REPORTS, ROOT / relative


def restore_file(archive, member, shard, seen_files):
    kind, destination = result_destination(shard, member)
    if destination in seen_files:
        raise ValueError(f"Test result occurs more than once: {destination}")
    seen_files.add(destination)
    destination.parent.mkdir(parents=True, exist_ok=True)
    with archive.extractfile(member) as source, destination.open("wb") as target:
        shutil.copyfileobj(source, target)
    return kind, destination


def restore_shard(directory, shard, seen_files):
    files = {REPORTS: [], COVERAGE: []}
    with tarfile.open(directory / archive_name(shard), "r:gz") as archive:
        manifest = json.load(archive.extractfile(MANIFEST_FILENAME))
        if manifest["shard"] != shard:
            raise ValueError(f"Unexpected test shard: {manifest['shard']}")
        for member in archive.getmembers():
            if member.name == MANIFEST_FILENAME:
                continue
            kind, destination = restore_file(archive, member, shard, seen_files)
            files[kind].append(destination)
    counts = summarize(files[REPORTS])
    if counts != manifest["counts"] or len(files[REPORTS]) != manifest[REPORTS]:
        raise ValueError(f"Test results are incomplete for {shard}")
    if not files[COVERAGE] or len(files[COVERAGE]) != manifest["coverage_files"]:
        raise ValueError(f"Coverage data is incomplete for {shard}")
    if not counts["tests"] or counts["failures"] or counts["errors"]:
        raise ValueError(f"Test shard did not pass: {shard}: {counts}")
    return counts


def restore():
    directory = ROOT / "target/ci-test-results"
    seen_files = set()
    totals = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
    for shard in SHARDS:
        counts = restore_shard(directory, shard, seen_files)
        for key in totals:
            totals[key] += counts[key]
        print(f"{shard}: {json.dumps(counts)}")
    print(f"Combined: {json.dumps(totals)}")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a") as handle:
            handle.write(
                f"Test shards: {totals['tests']} tests, "
                f"{totals['failures']} failures, {totals['errors']} errors, "
                f"{totals['skipped']} skipped.\n"
            )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    projects_parser = commands.add_parser("projects")
    projects_parser.add_argument("shard", choices=SHARDS)
    collect_parser = commands.add_parser("collect")
    collect_parser.add_argument("shard", choices=SHARDS)
    commands.add_parser("restore")
    args = parser.parse_args()
    if args.command == "projects":
        print(projects(args.shard))
    elif args.command == "collect":
        collect(args.shard)
    else:
        restore()
