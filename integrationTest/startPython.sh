#!/bin/bash
echo "Running python unit tests" 
export PYTHONPATH=.
python -m unittest discover -p '*_tests.py'
unset PYTHONPATH
echo "End of python unit tests"