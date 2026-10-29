import contextlib
import importlib.util
import io
import json
import os
from pathlib import Path
import runpy
import shutil
import sys
import tarfile
import tempfile
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET


SCRIPT = Path(__file__).resolve().parents[2] / ".github/scripts/ci_test_results.py"
SPEC = importlib.util.spec_from_file_location("ci_test_results", SCRIPT)
CI = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(CI)


class TestCIResults(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.addCleanup(patch.stopall)
        patch.object(CI, "ROOT", self.root).start()
        patch.dict(os.environ, {"GITHUB_STEP_SUMMARY": ""}).start()
        self.output = io.StringIO()
        redirect = contextlib.redirect_stdout(self.output)
        redirect.__enter__()
        self.addCleanup(redirect.__exit__, None, None, None)
        self.modules = {"engine": "activiti-engine", "spring": "activiti-spring-boot-starter",
                        "other": "activiti-api-process-model"}
        namespace = CI.NAMESPACE["m"]
        project = ET.Element("project", xmlns=namespace)
        ET.SubElement(project, "artifactId").text = "parent"
        modules = ET.SubElement(project, "modules")
        for directory, artifact_id in self.modules.items():
            ET.SubElement(modules, "module").text = directory
            target = self.root / directory
            target.mkdir()
            child = ET.Element("project", xmlns=namespace)
            ET.SubElement(child, "artifactId").text = artifact_id
            ET.ElementTree(child).write(target / "pom.xml")
        ET.ElementTree(project).write(self.root / "pom.xml")

    def report(self, shard, failures=0, errors=0, skipped=0):
        target = self.root / shard / "target"
        reports = target / "surefire-reports"
        reports.mkdir(parents=True)
        suite = ET.Element("testsuite", tests="1", failures=str(failures),
                           errors=str(errors), skipped=str(skipped))
        case = ET.SubElement(suite, "testcase", classname=f"sample.{shard}", name="runs")
        if skipped:
            ET.SubElement(case, "skipped")
        report = reports / f"TEST-sample.{shard}.xml"
        ET.ElementTree(suite).write(report)
        (target / "jacoco.exec").write_bytes(shard.encode())
        return report

    def package_all(self, failed=None):
        for shard in CI.SHARDS:
            self.report(shard, failures=int(shard == failed))
            CI.collect(shard)
            shutil.rmtree(self.root / shard / "target")
        destination = self.root / "target/ci-test-results"
        shutil.copytree(self.root / "target/ci-test-artifacts", destination)
        return destination

    def rewrite_archive(self, shard, modify):
        path = self.root / "target/ci-test-results" / CI.archive_name(shard)
        with tarfile.open(path, "r:gz") as archive:
            entries = [(m, archive.extractfile(m).read()) for m in archive.getmembers()]
        with tarfile.open(path, "w:gz") as archive:
            for member, content in modify(entries):
                member.size = len(content)
                archive.addfile(member, io.BytesIO(content))

    def test_round_trip_preserves_every_report_and_isolates_coverage(self):
        self.package_all()
        summary = self.root / "summary.md"
        with patch.dict(os.environ, {"GITHUB_STEP_SUMMARY": str(summary)}):
            CI.restore()
        for shard in CI.SHARDS:
            report = self.root / shard / f"target/surefire-reports/TEST-sample.{shard}.xml"
            self.assertEqual(ET.parse(report).getroot().get("tests"), "1")
            coverage = self.root / "target/ci-coverage" / shard / shard / "target/jacoco.exec"
            self.assertEqual(coverage.read_bytes(), shard.encode())
        self.assertIn("3 tests, 0 failures, 0 errors, 0 skipped", summary.read_text())

    def test_every_leaf_module_is_assigned_to_one_group(self):
        for shard in CI.SHARDS:
            self.assertEqual(CI.projects(shard), shard)
        with patch.object(CI, "module_shard", return_value="other"):
            with self.assertRaisesRegex(ValueError, "No Maven modules"):
                CI.projects("engine")

    def test_collect_rejects_another_groups_reports(self):
        self.report("spring")
        with self.assertRaisesRegex(ValueError, "another shard"):
            CI.collect("engine")

    def test_empty_group_cannot_pass(self):
        with self.assertRaisesRegex(ValueError, "No tests or coverage"):
            CI.collect("engine")

    def test_archive_output_is_restricted_to_known_groups(self):
        with self.assertRaisesRegex(ValueError, "Unknown test shard"):
            CI.collect("../../../outside")
        self.assertFalse((self.root / "outside").exists())

    def test_failed_test_results_cannot_pass_analysis(self):
        self.package_all(failed="engine")
        with self.assertRaisesRegex(ValueError, "did not pass"):
            CI.restore()

    def test_missing_group_cannot_pass_analysis(self):
        directory = self.package_all()
        (directory / CI.archive_name("spring")).unlink()
        with self.assertRaises(FileNotFoundError):
            CI.restore()

    def test_incomplete_reports_or_coverage_cannot_pass_analysis(self):
        self.package_all()
        original = (self.root / "target/ci-test-results" / CI.archive_name("engine")).read_bytes()
        for prefix, message in [("reports/", "Test results are incomplete"),
                                ("coverage/", "Coverage data is incomplete")]:
            with self.subTest(prefix=prefix):
                path = self.root / "target/ci-test-results" / CI.archive_name("engine")
                path.write_bytes(original)
                self.rewrite_archive("engine", lambda entries: [
                    (m, data) for m, data in entries if not m.name.startswith(prefix)
                ])
                with self.assertRaisesRegex(ValueError, message):
                    CI.restore()

    def test_incorrect_group_manifest_is_rejected(self):
        self.package_all()
        def change(entries):
            for member, data in entries:
                if member.name == CI.MANIFEST_FILENAME:
                    manifest = json.loads(data)
                    manifest["shard"] = "other"
                    data = json.dumps(manifest).encode()
                yield member, data
        self.rewrite_archive("engine", change)
        with self.assertRaisesRegex(ValueError, "Unexpected test shard"):
            CI.restore()

    def test_duplicate_archive_members_are_rejected(self):
        self.package_all()
        def duplicate(entries):
            yield from entries
            yield next(x for x in entries if x[0].name.startswith("reports/"))
        self.rewrite_archive("engine", duplicate)
        with self.assertRaisesRegex(ValueError, "more than once"):
            CI.restore()

    def test_archive_paths_cannot_escape_or_restore_unexpected_files(self):
        for name in ["/absolute.exec", "../outside", "coverage/../../outside.exec",
                     "coverage", "unexpected/file", "reports/engine/target/log.txt"]:
            with self.subTest(name=name):
                member = tarfile.TarInfo(name)
                with self.assertRaises(ValueError):
                    CI.result_destination("engine", member)
        link = tarfile.TarInfo("coverage/link.exec")
        link.type = tarfile.SYMTYPE
        with self.assertRaises(ValueError):
            CI.result_destination("engine", link)
        member = tarfile.TarInfo("reports/spring/target/surefire-reports/TEST-sample.xml")
        with self.assertRaisesRegex(ValueError, "wrong shard"):
            CI.result_destination("engine", member)

    def test_skipped_tests_are_preserved_in_summary(self):
        report = self.report("engine", skipped=1)
        self.assertEqual(CI.summarize([report]), {
            "tests": 1, "failures": 0, "errors": 0, "skipped": 1
        })

    def test_cli_projects_collect_and_restore(self):
        previous = Path.cwd()
        os.chdir(self.root)
        self.addCleanup(os.chdir, previous)
        with patch.object(sys, "argv", [str(SCRIPT), "projects", "engine"]):
            runpy.run_path(str(SCRIPT), run_name="__main__")
        for shard in CI.SHARDS:
            self.report(shard)
            with patch.object(sys, "argv", [str(SCRIPT), "collect", shard]):
                runpy.run_path(str(SCRIPT), run_name="__main__")
            shutil.rmtree(self.root / shard / "target")
        shutil.copytree(self.root / "target/ci-test-artifacts", self.root / "target/ci-test-results")
        with patch.object(sys, "argv", [str(SCRIPT), "restore"]):
            runpy.run_path(str(SCRIPT), run_name="__main__")
        with patch.object(sys, "argv", [str(SCRIPT), "collect", "engine", "../outside"]):
            with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
                runpy.run_path(str(SCRIPT), run_name="__main__")


if __name__ == "__main__":
    unittest.main()
