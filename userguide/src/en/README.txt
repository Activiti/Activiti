Tooling
-------


Install Asciidoctor: http://asciidoctor.org/

We're using the 'pygments' library for syntax highlighting. This needs to be installed: gem install pygments.rb



Generate HTML docs
------------------

./generate-html.sh



Advanced
--------

Themes in asciidoc are managed by the 'stylesheet factory'.
See themes in action: http://themes.asciidoctor.org/preview/

Clone https://github.com/asciidoctor/asciidoctor-stylesheet-factory and follow instructions there to build the themes.
The resulting css and images are copied to the docs folder.