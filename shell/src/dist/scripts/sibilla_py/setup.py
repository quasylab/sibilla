from setuptools import setup
from glob import glob

setup(
    name='sibilla',
    version='1.0',
    packages=['sibilla'],
    data_files=[
        ('libs', glob('../../build/install/sshell/lib/**'))
    ],
    install_requires=[
        'Cython',
        'pyjnius',
        'multimethod',
    ],
)

