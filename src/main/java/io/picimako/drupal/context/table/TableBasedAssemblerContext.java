package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object storing data during the data table based content assembly.
 */
@Getter
@Setter
public class TableBasedAssemblerContext {
    private ComponentNode previousComponentNode = ComponentNode.ABSENT;
}
