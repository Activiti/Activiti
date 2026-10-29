"""Transfer PR test reports and isolated JaCoCo data between CI jobs."""

import argparse
import io
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import tarfile
import xml.etree.ElementTree as ET


ROOT = Path.cwd()
ENGINE = Path("activiti-core/activiti-engine")
SHARDS = ("engine", "other")
REPORT_DIRECTORIES = ("surefire-reports", "failsafe-reports")


def summarize(reports):
    counts = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
    for report in reports:
        suite = ET.parse(report).getroot()
        for key in counts:
            counts[key] += int(suite.get(key, "0"))
    return counts


def collect(shard, output):
    reports = sorted(
        report
        for directory in REPORT_DIRECTORIES
        for report in ROOT.glob(f"**/target/{directory}/TEST-*.xml")
    )
    for report in reports:
        in_engine = report.relative_to(ROOT).is_relative_to(ENGINE)
        if in_engine != (shard == "engine"):
            raise ValueError(f"Test report belongs to the other shard: {report}")
    coverage = sorted(ROOT.glob("**/target/*.exec"))
    counts = summarize(reports)
    manifest = {
        "shard": shard,
        "counts": counts,
        "reports": len(reports),
        "coverage_files": len(coverage),
    }
    data = json.dumps(manifest).encode()
    output.parent.mkdir(parents=True, exist_ok=True)
    with tarfile.open(output, "w:gz") as archive:
        header = tarfile.TarInfo("manifest.json")
        header.size = len(data)
        archive.addfile(header, io.BytesIO(data))
        for prefix, files in (("reports", reports), ("coverage", coverage)):
            for path in files:
                archive.add(path, arcname=f"{prefix}/{path.relative_to(ROOT).as_posix()}")
    print(json.dumps(manifest))
    if not counts["tests"] or not coverage:
        raise ValueError(f"No tests or coverage were produced for {shard}")


def restore(directory):
    seen_reports = set()
    totals = dict.fromkeys(("tests", "failures", "errors", "skipped"), 0)
    for shard in SHARDS:
        reports = []
        coverage_files = 0
        with tarfile.open(directory / f"test-results-{shard}.tar.gz", "r:gz") as archive:
            manifest = json.load(archive.extractfile("manifest.json"))
            if manifest["shard"] != shard:
                raise ValueError(f"Unexpected test shard: {manifest['shard']}")
            for member in archive.getmembers():
                if member.name == "manifest.json":
                    continue
                path = PurePosixPath(member.name)
                if (
                    not member.isfile()
                    or path.is_absolute()
                    or ".." in path.parts
                    or len(path.parts) < 2
                ):
                    raise ValueError(f"Invalid test result path: {member.name}")
                relative = Path(*path.parts[1:])
                if path.parts[0] == "coverage" and relative.suffix == ".exec":
                    destination = ROOT / "target/ci-coverage" / shard / relative
                    coverage_files += 1
                elif (
                    path.parts[0] == "reports"
                    and relative.parent.name in REPORT_DIRECTORIES
                    and relative.name.startswith("TEST-")
                    and relative.suffix == ".xml"
                ):
                    if relative in seen_reports:
                        raise ValueError(f"Test report occurs in both shards: {relative}")
                    in_engine = relative.is_relative_to(ENGINE)
                    if in_engine != (shard == "engine"):
                        raise ValueError(f"Test report is in the wrong shard: {relative}")
                    seen_reports.add(relative)
                    destination = ROOT / relative
                    reports.append(destination)
                else:
                    raise ValueError(f"Unexpected test result: {member.name}")
                destination.parent.mkdir(parents=True, exist_ok=True)
                with archive.extractfile(member) as source, destination.open("wb") as target:
                    shutil.copyfileobj(source, target)
        counts = summarize(reports)
        if counts != manifest["counts"] or len(reports) != manifest["reports"]:
            raise ValueError(f"Test results are incomplete for {shard}")
        if not coverage_files or coverage_files != manifest["coverage_files"]:
            raise ValueError(f"Coverage data is incomplete for {shard}")
        if not counts["tests"] or counts["failures"] or counts["errors"]:
            raise ValueError(f"Test shard did not pass: {shard}: {counts}")
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
    collect_parser = commands.add_parser("collect")
    collect_parser.add_argument("shard", choices=SHARDS)
    collect_parser.add_argument("output", type=Path)
    restore_parser = commands.add_parser("restore")
    restore_parser.add_argument("directory", type=Path)
    args = parser.parse_args()
    if args.command == "collect":
        collect(args.shard, args.output)
    else:
        restore(args.directory)
