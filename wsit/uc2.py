pkg = {
    "name"          : "metro",
    "version"       : "1.4,0-",
    "attributes"    : { 
        "description"      : "Metro Web Services Stack for GlassFish",
        "description_long" : "Metro is a high-performance, extensible, \
easy-to-use web service stack. It is a one-stop shop for all your web service \
needs, from the simplest hello world web service to reliable, secured, and \
transacted web service that involves .NET services."
      },
    "depends"       : { "pkg:/glassfish-web" : {"type" : "require" } },
    "dirtrees"      : [ "glassfish" ],
    "licenses"      : { "glassfish/modules/Metro-LICENSE.txt"    : {"license" : "Metro-LICENSE.txt" } },
}