#!/bin/bash
cd "${0%/*}"

git fetch upstream
git checkout master
git rebase upstream/master
git checkout rune-street
git rebase master
git push -f
