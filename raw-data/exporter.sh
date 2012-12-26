#!/bin/bash

# converts SVG files into atlases. expects to be called in this directory
# with the name of the directory containing the SVG files as an argument.
# libgdx must be in the classpath, inkscape must be on the search path.

rm -r $1/*.png
rm ../assets/textures/$1.*

for i in scaleable-art/menuscreen/*.svg; 
  do inkscape -e $1/`basename $i .svg`.png $i; 
done

java com.badlogic.gdx.tools.imagepacker.TexturePacker2 $1 ../assets/textures/ $1.atlas
