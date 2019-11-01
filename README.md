# Drupal Paragraph Context based content creation

## Purpose of this project

This project is rather a case study, and I can image using it only in specific cases but the idea behind it might be
more important and more reusable than this implementation of it.

If you want to use any part of this library, feel free to do so, there is no licencing involved.

## Background and how this works

This idea came up during and a similar solution was originally made for a Drupal/PHP based project
which used Paragraphs and Modifiers to build certain types of pages.

It turned out that creating simple and meaningful CSS selectors is not the easiest thing in the native Drupal
editor because most of the HTML classes, ids and such are generated and don't really give meaningful context. Also when
targeting a certain button, dropdown, etc. it can happen that different buttons have the same selectors without their
context, so simply targeting that single element is not enough.

That's where this project comes in. The initial idea and solution was to handle each item on the editor form contextually.
In order to be able to properly target them, construct a CSS selector that also includes selectors for their parent elements.

In its initial form it needed manual insertion of gherkin steps configuring this context before each gherkin step
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

However we reached a point when we needed to validate complex nested components like carousels and galleries, it
became harder to read gherkin files, and they got even longer due to the fact how many steps it involves to
have a scenario creating such nested components.

I know BDD is not necessarily the best option to implement tests for a CMS but the client didn't care and we found BDD
tests easier to read and maintain.  

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
  """
```

The underlying logic no more uses the context selector path (*CONTAINER > LAYOUT > IMAGE*), instead it construct
directly the CSS selector from the tree defined above when it is being traversed.

The logic differentiates the following kind of nodes:
- **Component node**: they represent Drupal Paragraph and Modifier types, and are used for building the component context
                  (saved in a component tree), and also signals to the parser that it should add a new component
                  at that point.
- **Configuration node**: they are basically a key-value mapping, so that Paragraphs can be configured based on them.
                      They are not saved in the component tree, they are used only to invoke configuration logic at certain
                      points.

Of course passing data from configuration nodes may need additional type conversion if they expect/use some of the parameter
type converters, or even more, data table type converters.

And to be able to do something with this whole library, the entry point is `ComponentTreeBasedContentAssembler.assembleContent(String)`.

## Configuration node format

The followings are configuration node values listing which ones are valid and which ones are invalid:

#### Valid values
**Unquoted**
- ---* url:
- (see the whitespace at the end)---* url:  
- ---* url: something
- ---* url: something, color:
- (see the whitespace at the end)---* url: something, color:   
- ---* url: something, color: rgba(0\\,0\\,0\\,0)

**Quoted**
- ---* url:" "
- ---* url:" something"
- ---* url:" something", color:
- ---* url:" something", color:" "
- ---* url:" something", color:" rgba(0\\,0\\,0\\,0)"

#### Invalid values

- ---*
- ---* 
- ---* ,
- ---* url
- ---* url: ,
- ---* url: something,
- (see the whitespace after the comma)---* url: something, 
- ---* url: something, color
