package org.picimako.drupal.context;

import io.cucumber.java.en.When;

/**
 * Step definitions class implementing some methods for adding different components to a Drupal page.
 * <p>
 * But this class or the place where you store the functionality that executes the component addition may not
 * necessarily need to be a step definitions class at all.
 */
public class DrupalPageSteps {

    @When("I work with {string}")
    public void i_work_with_X(String contextSelector) {
        //Contains custom logic for setting the context selector
        //The argument contextSelector String may or may not be parameter type converted (from context path to CSS
        // selector), it is up to the user to decide how to handle it.
    }

    @When("I add a Container")
    public void i_add_a_container() {
        //Contains custom logic for adding a container
    }

    @When("I add a Layout")
    public void i_add_a_layout() {
        //Contains custom logic for adding a layout
    }

    @When("I add {} component")
    public void i_add_X_component(NodeType nodeType) {
        //Contains custom logic for adding a component based on the argument node type
    }
}
