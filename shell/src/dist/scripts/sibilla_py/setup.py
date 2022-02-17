from setuptools import setup
from glob import glob

setup(
    name='sibilla',
    version='1.0',
    packages=['sibilla'],
    install_requires=[
        'Cython',
        'pyjnius',
        'plotly',
    ],
)

