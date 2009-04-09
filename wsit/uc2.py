pkg = {
    "name"          : "metro",
    "version"       : "@{version}",
    "attributes"    : { 
        "pkg.summary"      : "Metro Web Services Stack for GlassFish",
        "pkg.description" : "Metro is a high-performance, extensible, \
easy-to-use web service stack. It is a one-stop shop for all your web service \
needs, from the simplest hello world web service to reliable, secured, and \
transacted web service that involves .NET services.",
        "info.classification" : "Web Services"
      },
    "dirtrees"      : [ "glassfish" ],
    "depends"       : { "pkg:/glassfish-web" : {"type" : "require" } },
    "licenses"      : { "glassfish/modules/Metro-LICENSE.txt"    : {"license" : "Metro-LICENSE.txt" } },
}
