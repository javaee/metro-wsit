#!/bin/sh
USAGE="Usage: setup-module.sh [-hvf] [-m <module-root> [-n <module-name>] [-i <module-id>] [-P <parent-name>]] [-p <pom-template>]"

# we want at least one parameter (it may be a flag or an argument)
if [ $# -le 1 ]; then
	echo $USAGE >&2
	exit 1
fi

# parse command line arguments
sm_metroGroupId=org.glassfish.metro
sm_verbose=
sm_forceRmFlag=
sm_moduleRoot=
sm_moduleName=
sm_moduleId=
sm_parentId=
sm_pomTemplate=

OPTIND=1
while getopts 'hvfm:n:i:P:p:' OPT; do
    case "$OPT" in
	h)  echo $USAGE
            exit 0
            ;;
	v)  sm_verbose="-v"
            ;;
	f)  sm_forceRmFlag="-f"
            ;;
	m)  sm_moduleRoot=$OPTARG
            ;;
	n)  sm_moduleName=$OPTARG
            ;;
	i)  sm_moduleId=$OPTARG
            ;;
	P)  sm_parentId=$OPTARG
            ;;
        p)  sm_pomTemplate=$OPTARG
            ;;
	?)  # all other characters - error
            echo $USAGE >&2
            exit 1
            ;;
    esac
done
shift `expr $OPTIND - 1`

# access additional parameters through $@ or $* as usual or using this loop:
# for PARAM; do
#    echo $PARAM
# done


if [ -z "$sm_moduleRoot" ] ; then
    echo "No module root specified" >&2
    echo $USAGE >&2
    exit 1
fi

echo " --> Creating module $sm_moduleName"

if [ ! -e "$sm_moduleRoot" ] ; then
    mkdir -p $sm_verbose $sm_moduleRoot
fi

if [ -z "$sm_pomTemplate" ] ; then
    echo "No pom.xml template specified"
else
    sed -e "s/@metro.groupId@/$sm_metroGroupId/g" -e "s/@module.id@/$sm_moduleId/g" -e "s/@module.name@/$sm_moduleName/g" -e "s/@parent.id@/$sm_parentId/g" < $sm_pomTemplate > $sm_moduleRoot/pom.xml
fi


