"""Acceptance-log regressions: launcher exit zero is not evidence of executed tests."""
import unittest
from verify_gametest_log import result

class GameTestLogGate(unittest.TestCase):
    def test_running_or_gradle_success_without_completion_is_rejected(self):
        self.assertFalse(result("321 tests are now running!\nBUILD SUCCESSFUL", 0)["passed"])
    def test_completed_suite_with_required_failures_is_rejected(self):
        self.assertFalse(result("321 GAME TESTS COMPLETE\n4 required tests failed", 0)["passed"])
    def test_completed_without_explicit_required_pass_is_rejected(self):
        self.assertFalse(result("321 GAME TESTS COMPLETE", 0)["passed"])
    def test_gradle_failure_remains_failure_even_if_tests_passed(self):
        self.assertFalse(result("321 GAME TESTS COMPLETE\nAll 321 required tests passed", 1)["passed"])
    def test_completed_passed_suite_is_accepted(self):
        report = result("========= 321 GAME TESTS COMPLETE =====\nAll 321 required tests passed :)", 0)
        self.assertTrue(report["passed"])
        self.assertEqual(report["completedTests"], 321)
    def test_optional_tests_do_not_require_equal_required_count(self):
        self.assertTrue(result("322 GAME TESTS COMPLETE\nAll 321 required tests passed", 0)["passed"])

if __name__ == "__main__":
    unittest.main()
