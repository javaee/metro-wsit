#!/bin/sh
USAGE="Usage: move-sources.sh [-hvfn] [<module-root> [<source-artifacts>] [<test-artifacts>] [<test-resources]]"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 0 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
MOVE_COMMAND="mv"
OPTIND=1
while getopts 'hvfn' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  VERBOSE="-v"
            ;;
	f)  FORCE_RM_FLAG="-f"
            ;;
	n)  MOVE_COMMAND="cp -R"
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
done
shift `expr $OPTIND - 1`

moduleRoot="$1"
srcArtifacts="$2"
testArtifacts="$3"
testResources="$4"

if [ ! -n "$moduleRoot" ] || [ ! -n "$srcArtifacts" ] && [ ! -n "$testArtifacts" ] && [ ! -n "$testResources" ] ; then
    echo $USAGE >&2
    exit 1
fi

continueChoice () {
    printf "$* Continue? [Y/n]"

    read -n 1 -rs choice # reading single character
    echo

    if [ "$choice" = "n" ] || [ "$choice" = "N" ] ; then
        echo "Stopping script..."
        exit 0;
    fi
}


message () {
    if [ -n "$VERBOSE" ] ; then
        echo "$*"
    fi
}

moveArtifacts () {
    if [ $# -le 2 ]; then
        echo "USAGE: move-artifacts <from> <to> <artifacts>" >&2
        return 1;
    fi

    from="$1"
    to="$2"
    artifacts="$3"

    #message "Moving artifacts from \"$from\" to \"$to\""
    message "Moving artifacts \"$artifacts\" from \"$from\" to \"$to\""

    for a in `echo "$artifacts" | tr "\:" " "`
    do
        message "  - $a"

        if [ ! -e $from/$a ] ; then
            message "    Skipping: Nothing to move - source not available"
            continue
        fi

        targetDir=`dirname "$to/$a"`

        if [ ! -e $targetDir ] ; then
            continueChoice "Creating \"$targetDir\" for artifact \"$a\""
            mkdir -p $VERBOSE $targetDir
        fi

        $MOVE_COMMAND $from/$a $targetDir/
    done
}

if [ -n "$srcArtifacts" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/src" "$moduleRoot/src/main/java" "$srcArtifacts"
fi

if [ -n "$testArtifacts" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/test/unit/src" "$moduleRoot/src/test/java" "$testArtifacts"
fi

if [ -n "$testResources" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/test/unit/data" "$moduleRoot/src/test/resources" "$testResources"
fi
