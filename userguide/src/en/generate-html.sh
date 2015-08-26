#!/bin/bash

asciidoctor -a stylesheet=colony.css index.adoc

asciidoctor -a stylesheer=colony.css migration.adoc