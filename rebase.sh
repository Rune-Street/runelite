#!/bin/bash
cd "${0%/*}"

git fetch upstream
git checkout master
git rebase upstream/master
#git push -f
git checkout rune-street
git rebase master
git commit --allow-empty -m "Rebase"
#git push -f
