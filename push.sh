#!/bin/bash
cd "${0%/*}"

git checkout master
git push -f
git checkout rune-street
git push -f
