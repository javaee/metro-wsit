#!/bin/sh
USAGE="Usage: move-sources.sh [-hvf] <module-root> [<source-artifacts>] [<test-artifacts>] [<test-resources]]"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 0 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
ms_verbose=
ms_forceRmFlag=

OPTIND=1
while getopts 'hvf' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  ms_verbose="-v"
            ;;
	f)  ms_forceRmFlag="-f"
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
    if [ -n "$ms_verbose" ] ; then
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

    message "Moving artifacts: $artifacts\nfrom: $from\nto  : $to\n"

    for a in `echo "$artifacts" | tr "\:" " "`
    do
        message "  - $a"

        if [ ! -e $from/$a ] ; then
            message "    Skipping: Nothing to move - source not available"
            continue
        fi

        targetDir=`dirname "$to/$a"`

        if [ ! -e $targetDir ] ; then
            mkdir -p $ms_verbose $targetDir
        fi

        mv $ms_verbose $from/$a $targetDir/
    done
}

if [ -n "$srcArtifacts" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/src" "$moduleRoot/src/main/java" "$srcArtifacts"
fi

for propertyFile in `find "$moduleRoot/src/main/java" -name "*.properties"`
do

    targetDir=`dirname "$moduleRoot/src/main/resources/"${propertyFile#"$moduleRoot/src/main/java/"}`
    message "Moving property resource:\n$propertyFile\nTo:\n$targetDir"
    if [ ! -e "$targetDir" ] ; then
        mkdir -p $ms_verbose $targetDir
    fi

    mv $ms_verbose $propertyFile "$targetDir/"
done

if [ -n "$testArtifacts" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/test/unit/src" "$moduleRoot/src/test/java" "$testArtifacts"
fi

if [ -n "$testResources" ] ; then
    moveArtifacts "$EXPORTED_RT_ROOT/test/unit/data" "$moduleRoot/src/test/resources" "$testResources"
fi
