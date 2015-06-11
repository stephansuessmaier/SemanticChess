package de.uni_leipzig.informatik.swp15_sc.junitTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * This class is a junit test suite that runs all named unit tests
 * @author Stephan Suessmaier
 *
 */
@RunWith(Suite.class)
@SuiteClasses({TtlIfaceTest.class, VirtuosoIfaceTest.class})
public class TestAll {
}
