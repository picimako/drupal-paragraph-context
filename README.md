# Drupal Paragraph Context based content creation

## Purpose of this project

This project is rather a case study, and I can image using it only in specific cases but the idea behind it might be
more important and more reusable than this implementation of it.

## Background and how this works

This idea came up during and a similar solution was originally made for a Drupal/PHP based project
which used Paragraphs and Modifiers to build certain types of pages.

It turned out that creating simple and meaningful CSS selectors is not the easiest thing in the native Drupal
editor because most of the HTML classes, ids and such are generated and don't really give meaningful context. Also when
targeting a certain button, dropdown, etc. it can happen that different buttons have the same selectors without their
context, so simply targeting that single element is not enough.

That's where this project comes in. The initial idea and solution was to handle each item on the editor form contextually.
In order to be able to properly target them, construct a CSS selector that also includes selectors for their parent elements.

In its initial form it needed manual insertion of Gherkin steps configuring this context before each step
that interacted with the editor. That looked something like this:

```gherkin
When I add an IMAGE component
And the context is CONTAINER > LAYOUT > IMAGE
And I select the 1st image from the library
And I configure the "https://duckduckgo.com" url in the image
When I add a RICH_TEXT component
And the context is CONTAINER > LAYOUT > RICH_TEXT
...
``` 

The step definitions that selected and configured a component and interacted with the elements of the a given paragraph
were updated in a way to use the last component context selector that was configured. So in this example both steps that
configure the image know about the context selector converted from `CONTAINER > LAYOUT > IMAGE`.

Though it may double the number of steps in gherkin files, and clutters them making it harder to see what is actually
relevant to the feature and functionality tested, for a while it worked just fine.

However we reached a point when we needed to validate complex nested components like carousels and galleries, and it
became harder to read gherkin files, and they got even longer due to the fact how many steps it involves to
have a scenario creating such nested components.

I know BDD may not necessarily the best option to implement tests for a CMS but the client didn't care and we found BDD
tests easier to read and maintain.

### Simplify with tree view based page creation 

Then came the idea to simplify the content creation big time, make the gherkin files much shorter, more readable, so
came this solution, to just write the structure of the content as a tree view (including the configuration of the
components), let it be parsed, and based on the nodes in the tree, execute the addition and configuration of the content
and the components in it.

As a result we still have a Selenium test creating a Drupal page but the gherkin files are much simpler and shorter.
The previous example would look something like this:

```gherkin
Given the following page
"""
- CONTAINER
-- LAYOUT    <- This a Component node.
--- IMAGE
---* index:1, url:https://duckduckgo.com  <- This is a Configuration node.
--- RICH_TEXT
----@ ABSOLUTE_HEIGHT_MODIFIER   <- This is also a Component Node but for a Modifier.
"""
```

To give you insight about all the possible combinations of Component and Configuration nodes, please refer to the example below:

```gherkin
Given the following page
"""
- CONTAINER <- This is a Component node at root (1) level.
-- LAYOUT
--- IMAGE   <- This is also a Component node at the 3rd level.
--- RICH_TEXT >> type:"Full HTML"     <- This is a Component node with an inline configuration.
---* text: some text
-- LAYOUT
--- CAROUSEL
---- VIDEO
----* url:https://some.url, initialTime:16    <- This is a Configuration node.
                                                 The level marker is only for consistency with the rest of the tree.
---- VIDEO
----* url:https://some.other/url
----* initialTime:10     <- Configurations can be defined in multiple rows for the same component, and are handled as separate Configuration nodes.
-----@ ABSOLUTE_HEIGHT_MODIFIER    <- This is a Modifier Component node for the last VIDEO component.
"""
```

The underlying logic no more uses the context selector path (*CONTAINER > LAYOUT > IMAGE*), instead it constructs
directly the CSS selector from the tree defined above when it is being traversed.

The logic differentiates the following two nodes:
- **Component node**: they represent Drupal Paragraphs and Modifiers, and are used for building the component context
                  (saved in a component tree), and also signals to the parser that it should add a new component
                  at that point. It can have an optional inline configuration (only for tree view based content layout)
                  as well which is a way to create more concise layouts.

- **Configuration node**: they are basically a key-value mapping, so that components can be configured based on them.
                      They are not saved in the component tree, they are used only to invoke configuration logic at certain
                      points.

Of course passing data from configuration nodes may need additional type conversion if they expect/use some of the parameter
type converters from the BDD library at hand, or even more, data table type converters.

And to be able to do something with this whole library, the entry point is `ComponentTreeBasedContentAssembler.assembleContent(String)`.

#### Configuration node format

The followings are configuration node values listing which ones are valid and which ones are invalid:

#### Valid values

**Unquoted**
- ---* url:
- ---* url: (with a whitespace at the end)
- \* url:something
- ---* url: something
- ---* url: something, color:
- ---* url: something, color: (with a whitespace at the end)
- ---* url: something, color: rgba(0\\,0\\,0\\,0)

**Quoted**
- ---* url:" "
- ---* url:" something"
- ---* url:" something", color:
- ---* url:" something", color:" "
- ---* url:" something", color:" rgba(0\\,0\\,0\\,0)"

#### Invalid values

- ---*
- ---* (with a whitespace at the end)
- ---* ,
- ---* url
- ---* url: ,
- ---* url: something,
- ---* url: something, (with a whitespace at the end)
- ---* url: something, color

## Data table based implementation

This is an alternate and more concise version of the tree view based implementation. It relies on passing a data table
to the entry point of this solution: `TableBasedContentAssembler.assembleContent(List)`.

In this table based variant it is clearer which configuration is for which component and may even halve the length of
such input data.

The table is expected to be in the following format:
```gherkin
| Component             | Configuration                         |  <- Header row
| <                     | title:"Some page title"               |  <- Root level configuration
|                       | meta-keywords:keywords                |
| > CONTAINER           | bg:#fff                               |  <- Component with configuration
| >> LAYOUT             |                                       |  <- Component without configuration
| >>> IMAGE             | name:some-image.png                   |
|                       | link:/some/path                       |  <- Configuration for the last defined component, in this case IMAGE
| >>> YOUTUBE_VIDEO     | title:"Good title", features:autoplay |  <- Component with multiple configurations at once
| >>>@ PADDING_MODIFIER | left:50px                             |  <- Modifier with configuration
```

There is support for root-level configuration by defining the component as `<` but it is considered valid only in the first
data row of the table.

There is also support for configurations to be defined in multiple table rows, in that case only the first occurrence
of the entry must include the component definition like this:

```gherkin
| > CONTAINER | bg:#fff             |
| >> LAYOUT   |                     |
| >>> IMAGE   | name:some-image.png |
|             | link:/some/path     |
```

The last two configurations are both applied to the same image component in this case.

However, if the table would be defined as:

```gherkin
| > CONTAINER | bg:#fff             |
| >> LAYOUT   |                     |
| >>> IMAGE   | name:some-image.png |
| >>> IMAGE   | link:/some/path     |
```

there would be two image components added to the layout, and the last two configurations would be applied to two
different image components.

The list of entries are provided to the content assembler class as a list of `ComponentAndConfiguration` objects for
which one might have to introduce a kind of data table type converter.

There is one another difference between this and the tree view based variants that in this case configurations
don't have a prefix at all. They are simply defined in the right-hand side column.

## Converters

It might happen that you want to migrate from one component layout representation to the other,
and for that purpose there is one converter for now to transition from the Tree View to the Table View layout.

**Usage:**
- download and copy the standalone `lib/converter/converter.jar` file to a folder
- copy the component layouts (without any Gherkin specific surroundings) to text files.
    - each tree view layout should be copied to a separate text file
- copy these text files into the folder where the jar file is
- run the jar file
    - it accepts one or more arguments, the filenames of the text files to convert
    - e.g. `java .jar converter.jar tree1.txt tree2.txt`

As a result it will create separate text files for each source file, each having its name prefixed with `converted_`.

The class that initiates the conversion is at `io.picimako.drupal.context.converter.TreeViewToDataTableConversionExecutor`,
while the class that contains the actual conversion logic is `io.picimako.drupal.context.converter.TreeViewToDataTableConverter`.

## Additional notes, caveats

- It is worth keeping in mind that depending on the structure of your project some classes may need to be moved to
different scope (test/compile), different packages or even modules to make them available properly.
- Passing complex data types such as tables in a configuration property is not possible in a proper way (only by inventing
some custom one-liner pattern which may be parsed accordingly).

## Credits

The idea for the data table version came from a friend of mine, so a big thank you goes to
[limpek07](https://github.com/Limpek07) for that.

## Future plans

For easy migration between the two formats:
- Create data table format to tree layout converter.
