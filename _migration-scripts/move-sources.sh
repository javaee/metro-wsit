#!/bin/sh
USAGE="Usage: `basename $0` [-hvf] [<module-root> [<source-artifacts>] [<test-artifacts>]]"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 0 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
while getopts hvf OPT; do
    case "$OPT" in
	h)	echo $USAGE
		exit 0
		;;
	v)	VERBOSE="-v"
		;;
	f)	FORCE_RM_FLAG="-f"
		;;
	?)	# getopts issues an error message
		echo $USAGE >&2
		exit 1
		;;
    esac
done

shift `expr $OPTIND - 1`

MODULE_ROOT=$1
SRC_ARTIFACTS=$2
TEST_ARTIFACTS=$3

if [ ! -n "$MODULE_ROOT" ] || [ ! -n "$SRC_ARTIFACTS" ] && [ ! -n "$TEST_ARTIFACTS" ] ; then
    echo $USAGE >&2
    exit 1
fi

