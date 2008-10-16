pkg = {
    "name"          : "metro",
    "version"       : "",
    "attributes"    : { 
        "pkg.summary"         :    "Metro Web Services Stack for GlassFish",
        "pkg.description"     :    "Metro is a high-performance, extensible, \
easy-to-use web service stack. It is a one-stop shop for all your web service \
needs, from the simplest hello world web service to reliable, secured, and \
transacted web service that involves .NET services.",
        "info.classification" : "Web Services"
      },
    "depends"       : { "pkg:/glassfish-web" : {"type" : "require" } },
    "dirtrees"      : [ "glassfish" ],
    "licenses"      : { "glassfish/modules/Metro-LICENSE.txt"    : {"license" : "CDDL+GPL" } },
}