package io.picimako.drupal.context.steps;

import javax.inject.Inject;

/**
 * Dummy step definitions class for configuring components, currently the Image component.
 */
public class DrupalConfigurationSteps {

    @Inject
    private ImageComponentSteps imageComponentSteps;
    @Inject
    private YouTubeComponentSteps youTubeComponentSteps;

    public ImageComponentSteps image() {
        return imageComponentSteps;
    }

    public YouTubeComponentSteps youtube() {
        return youTubeComponentSteps;
    }
}
