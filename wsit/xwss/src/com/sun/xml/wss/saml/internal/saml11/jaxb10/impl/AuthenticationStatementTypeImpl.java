//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2005.09.05 at 03:09:41 PM IST 
//


package com.sun.xml.wss.saml.internal.saml11.jaxb10.impl;

public class AuthenticationStatementTypeImpl
    extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl
    implements com.sun.xml.wss.saml.internal.saml11.jaxb10.AuthenticationStatementType, com.sun.xml.bind.JAXBObject, com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.UnmarshallableObject, com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.XMLSerializable, com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.ValidatableObject
{

    protected com.sun.xml.bind.util.ListImpl _AuthorityBinding;
    protected java.util.Calendar _AuthenticationInstant;
    protected com.sun.xml.wss.saml.internal.saml11.jaxb10.SubjectLocalityType _SubjectLocality;
    protected java.lang.String _AuthenticationMethod;
    public final static java.lang.Class version = (com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.JAXBVersion.class);
    private static com.sun.msv.grammar.Grammar schemaFragment;

    private final static java.lang.Class PRIMARY_INTERFACE_CLASS() {
        return (com.sun.xml.wss.saml.internal.saml11.jaxb10.AuthenticationStatementType.class);
    }

    protected com.sun.xml.bind.util.ListImpl _getAuthorityBinding() {
        if (_AuthorityBinding == null) {
            _AuthorityBinding = new com.sun.xml.bind.util.ListImpl(new java.util.ArrayList());
        }
        return _AuthorityBinding;
    }

    public java.util.List getAuthorityBinding() {
        return _getAuthorityBinding();
    }

    public java.util.Calendar getAuthenticationInstant() {
        return _AuthenticationInstant;
    }

    public void setAuthenticationInstant(java.util.Calendar value) {
        _AuthenticationInstant = value;
    }

    public com.sun.xml.wss.saml.internal.saml11.jaxb10.SubjectLocalityType getSubjectLocality() {
        return _SubjectLocality;
    }

    public void setSubjectLocality(com.sun.xml.wss.saml.internal.saml11.jaxb10.SubjectLocalityType value) {
        _SubjectLocality = value;
    }

    public java.lang.String getAuthenticationMethod() {
        return _AuthenticationMethod;
    }

    public void setAuthenticationMethod(java.lang.String value) {
        _AuthenticationMethod = value;
    }

    public com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.UnmarshallingEventHandler createUnmarshaller(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.UnmarshallingContext context) {
        return new com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.Unmarshaller(context);
    }

    public void serializeBody(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx1 = 0;
        final int len1 = ((_AuthorityBinding == null)? 0 :_AuthorityBinding.size());
        super.serializeBody(context);
        if (_SubjectLocality!= null) {
            if (_SubjectLocality instanceof javax.xml.bind.Element) {
                context.childAsBody(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
            } else {
                context.startElement("urn:oasis:names:tc:SAML:1.0:assertion", "SubjectLocality");
                context.childAsURIs(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
                context.endNamespaceDecls();
                context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
                context.endAttributes();
                context.childAsBody(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
                context.endElement();
            }
        }
        while (idx1 != len1) {
            if (_AuthorityBinding.get(idx1) instanceof javax.xml.bind.Element) {
                context.childAsBody(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx1 ++)), "AuthorityBinding");
            } else {
                context.startElement("urn:oasis:names:tc:SAML:1.0:assertion", "AuthorityBinding");
                int idx_2 = idx1;
                context.childAsURIs(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx_2 ++)), "AuthorityBinding");
                context.endNamespaceDecls();
                int idx_3 = idx1;
                context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx_3 ++)), "AuthorityBinding");
                context.endAttributes();
                context.childAsBody(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx1 ++)), "AuthorityBinding");
                context.endElement();
            }
        }
    }

    public void serializeAttributes(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx1 = 0;
        final int len1 = ((_AuthorityBinding == null)? 0 :_AuthorityBinding.size());
        context.startAttribute("", "AuthenticationInstant");
        try {
            context.text(com.sun.msv.datatype.xsd.DateTimeType.theInstance.serializeJavaObject(((java.util.Calendar) _AuthenticationInstant), null), "AuthenticationInstant");
        } catch (java.lang.Exception e) {
            com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        context.startAttribute("", "AuthenticationMethod");
        try {
            context.text(((java.lang.String) _AuthenticationMethod), "AuthenticationMethod");
        } catch (java.lang.Exception e) {
            com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.Util.handlePrintConversionException(this, e, context);
        }
        context.endAttribute();
        super.serializeAttributes(context);
        if (_SubjectLocality!= null) {
            if (_SubjectLocality instanceof javax.xml.bind.Element) {
                context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
            }
        }
        while (idx1 != len1) {
            if (_AuthorityBinding.get(idx1) instanceof javax.xml.bind.Element) {
                context.childAsAttributes(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx1 ++)), "AuthorityBinding");
            } else {
                idx1 += 1;
            }
        }
    }

    public void serializeURIs(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.XMLSerializer context)
        throws org.xml.sax.SAXException
    {
        int idx1 = 0;
        final int len1 = ((_AuthorityBinding == null)? 0 :_AuthorityBinding.size());
        super.serializeURIs(context);
        if (_SubjectLocality!= null) {
            if (_SubjectLocality instanceof javax.xml.bind.Element) {
                context.childAsURIs(((com.sun.xml.bind.JAXBObject) _SubjectLocality), "SubjectLocality");
            }
        }
        while (idx1 != len1) {
            if (_AuthorityBinding.get(idx1) instanceof javax.xml.bind.Element) {
                context.childAsURIs(((com.sun.xml.bind.JAXBObject) _AuthorityBinding.get(idx1 ++)), "AuthorityBinding");
            } else {
                idx1 += 1;
            }
        }
    }

    public java.lang.Class getPrimaryInterface() {
        return (com.sun.xml.wss.saml.internal.saml11.jaxb10.AuthenticationStatementType.class);
    }

    public com.sun.msv.verifier.DocumentDeclaration createRawValidator() {
        if (schemaFragment == null) {
            schemaFragment = com.sun.xml.bind.validator.SchemaDeserializer.deserialize((
 "\u00ac\u00ed\u0000\u0005sr\u0000\u001fcom.sun.msv.grammar.SequenceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.su"
+"n.msv.grammar.BinaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0004exp1t\u0000 Lcom/sun/msv/gra"
+"mmar/Expression;L\u0000\u0004exp2q\u0000~\u0000\u0002xr\u0000\u001ecom.sun.msv.grammar.Expressi"
+"on\u00f8\u0018\u0082\u00e8N5~O\u0002\u0000\u0002L\u0000\u0013epsilonReducibilityt\u0000\u0013Ljava/lang/Boolean;L\u0000\u000b"
+"expandedExpq\u0000~\u0000\u0002xpppsq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0000ppsq\u0000~\u0000\u0000ppsr\u0000\u001dcom.sun.msv."
+"grammar.ChoiceExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0001ppsr\u0000\'com.sun.msv.grammar."
+"trex.ElementPattern\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\tnameClasst\u0000\u001fLcom/sun/msv/gr"
+"ammar/NameClass;xr\u0000\u001ecom.sun.msv.grammar.ElementExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000"
+"\u0002Z\u0000\u001aignoreUndeclaredAttributesL\u0000\fcontentModelq\u0000~\u0000\u0002xq\u0000~\u0000\u0003pp\u0000s"
+"q\u0000~\u0000\tppsr\u0000 com.sun.msv.grammar.OneOrMoreExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001cco"
+"m.sun.msv.grammar.UnaryExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\u0003expq\u0000~\u0000\u0002xq\u0000~\u0000\u0003sr\u0000\u0011ja"
+"va.lang.Boolean\u00cd r\u0080\u00d5\u009c\u00fa\u00ee\u0002\u0000\u0001Z\u0000\u0005valuexp\u0000psr\u0000 com.sun.msv.gramma"
+"r.AttributeExp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\u0003expq\u0000~\u0000\u0002L\u0000\tnameClassq\u0000~\u0000\fxq\u0000~\u0000\u0003q"
+"\u0000~\u0000\u0014psr\u00002com.sun.msv.grammar.Expression$AnyStringExpression\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003sq\u0000~\u0000\u0013\u0001q\u0000~\u0000\u0018sr\u0000 com.sun.msv.grammar.AnyNameC"
+"lass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\u001dcom.sun.msv.grammar.NameClass\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000x"
+"psr\u00000com.sun.msv.grammar.Expression$EpsilonExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000"
+"\u0001\u0002\u0000\u0000xq\u0000~\u0000\u0003q\u0000~\u0000\u0019q\u0000~\u0000\u001esr\u0000#com.sun.msv.grammar.SimpleNameClass\u0000"
+"\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0002L\u0000\tlocalNamet\u0000\u0012Ljava/lang/String;L\u0000\fnamespaceURIq\u0000"
+"~\u0000 xq\u0000~\u0000\u001bt\u00003com.sun.xml.wss.saml.internal.saml11.jaxb10.Subj"
+"ectt\u0000+http://java.sun.com/jaxb/xjc/dummy-elementssq\u0000~\u0000\u000bpp\u0000sq"
+"\u0000~\u0000\u0000ppsq\u0000~\u0000\u000bpp\u0000sq\u0000~\u0000\tppsq\u0000~\u0000\u0010q\u0000~\u0000\u0014psq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~"
+"\u0000\u001esq\u0000~\u0000\u001ft\u00007com.sun.xml.wss.saml.internal.saml11.jaxb10.Subje"
+"ctTypeq\u0000~\u0000#sq\u0000~\u0000\tppsq\u0000~\u0000\u0015q\u0000~\u0000\u0014psr\u0000\u001bcom.sun.msv.grammar.DataE"
+"xp\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\u0002dtt\u0000\u001fLorg/relaxng/datatype/Datatype;L\u0000\u0006excep"
+"tq\u0000~\u0000\u0002L\u0000\u0004namet\u0000\u001dLcom/sun/msv/util/StringPair;xq\u0000~\u0000\u0003ppsr\u0000\"com"
+".sun.msv.datatype.xsd.QnameType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000*com.sun.msv.da"
+"tatype.xsd.BuiltinAtomicType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000%com.sun.msv.datat"
+"ype.xsd.ConcreteType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000\'com.sun.msv.datatype.xsd."
+"XSDatatypeImpl\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0003L\u0000\fnamespaceUriq\u0000~\u0000 L\u0000\btypeNameq\u0000~\u0000"
+" L\u0000\nwhiteSpacet\u0000.Lcom/sun/msv/datatype/xsd/WhiteSpaceProcess"
+"or;xpt\u0000 http://www.w3.org/2001/XMLSchemat\u0000\u0005QNamesr\u00005com.sun."
+"msv.datatype.xsd.WhiteSpaceProcessor$Collapse\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000,"
+"com.sun.msv.datatype.xsd.WhiteSpaceProcessor\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xpsr\u0000"
+"0com.sun.msv.grammar.Expression$NullSetExpression\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000"
+"xq\u0000~\u0000\u0003q\u0000~\u0000\u0014psr\u0000\u001bcom.sun.msv.util.StringPair\u00d0t\u001ejB\u008f\u008d\u00a0\u0002\u0000\u0002L\u0000\tloc"
+"alNameq\u0000~\u0000 L\u0000\fnamespaceURIq\u0000~\u0000 xpq\u0000~\u00009q\u0000~\u00008sq\u0000~\u0000\u001ft\u0000\u0004typet\u0000)h"
+"ttp://www.w3.org/2001/XMLSchema-instanceq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\u0007Subjec"
+"tt\u0000%urn:oasis:names:tc:SAML:1.0:assertionsq\u0000~\u0000\tppsq\u0000~\u0000\tq\u0000~\u0000\u0014"
+"psq\u0000~\u0000\u000bq\u0000~\u0000\u0014p\u0000sq\u0000~\u0000\tppsq\u0000~\u0000\u0010q\u0000~\u0000\u0014psq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~\u0000"
+"\u001esq\u0000~\u0000\u001ft\u0000;com.sun.xml.wss.saml.internal.saml11.jaxb10.Subjec"
+"tLocalityq\u0000~\u0000#sq\u0000~\u0000\u000bq\u0000~\u0000\u0014p\u0000sq\u0000~\u0000\u0000ppsq\u0000~\u0000\u000bpp\u0000sq\u0000~\u0000\tppsq\u0000~\u0000\u0010q\u0000"
+"~\u0000\u0014psq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000?com.sun.xml.wss.saml"
+".internal.saml11.jaxb10.SubjectLocalityTypeq\u0000~\u0000#sq\u0000~\u0000\tppsq\u0000~"
+"\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u00001q\u0000~\u0000Aq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000\u000fSubjectLocalityq\u0000~\u0000Fq\u0000~\u0000\u001esq\u0000"
+"~\u0000\tppsq\u0000~\u0000\u0010q\u0000~\u0000\u0014psq\u0000~\u0000\tq\u0000~\u0000\u0014psq\u0000~\u0000\u000bq\u0000~\u0000\u0014p\u0000sq\u0000~\u0000\tppsq\u0000~\u0000\u0010q\u0000~\u0000"
+"\u0014psq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~\u0000\u001esq\u0000~\u0000\u001ft\u0000<com.sun.xml.wss.saml.i"
+"nternal.saml11.jaxb10.AuthorityBindingq\u0000~\u0000#sq\u0000~\u0000\u000bq\u0000~\u0000\u0014p\u0000sq\u0000~"
+"\u0000\u0000ppsq\u0000~\u0000\u000bpp\u0000sq\u0000~\u0000\tppsq\u0000~\u0000\u0010q\u0000~\u0000\u0014psq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u0000\u0018q\u0000~\u0000\u001cq\u0000~\u0000\u001e"
+"sq\u0000~\u0000\u001ft\u0000@com.sun.xml.wss.saml.internal.saml11.jaxb10.Authori"
+"tyBindingTypeq\u0000~\u0000#sq\u0000~\u0000\tppsq\u0000~\u0000\u0015q\u0000~\u0000\u0014pq\u0000~\u00001q\u0000~\u0000Aq\u0000~\u0000\u001esq\u0000~\u0000\u001ft"
+"\u0000\u0010AuthorityBindingq\u0000~\u0000Fq\u0000~\u0000\u001esq\u0000~\u0000\u0015ppsq\u0000~\u0000.ppsr\u0000%com.sun.msv."
+"datatype.xsd.DateTimeType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000xr\u0000)com.sun.msv.datatype"
+".xsd.DateTimeBaseType\u0014W\u001a@3\u00a5\u00b4\u00e5\u0002\u0000\u0000xq\u0000~\u00003q\u0000~\u00008t\u0000\bdateTimeq\u0000~\u0000<q"
+"\u0000~\u0000>sq\u0000~\u0000?q\u0000~\u0000uq\u0000~\u00008sq\u0000~\u0000\u001ft\u0000\u0015AuthenticationInstantt\u0000\u0000sq\u0000~\u0000\u0015p"
+"psq\u0000~\u0000.ppsr\u0000#com.sun.msv.datatype.xsd.AnyURIType\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0000x"
+"q\u0000~\u00003q\u0000~\u00008t\u0000\u0006anyURIq\u0000~\u0000<q\u0000~\u0000>sq\u0000~\u0000?q\u0000~\u0000~q\u0000~\u00008sq\u0000~\u0000\u001ft\u0000\u0014Authen"
+"ticationMethodq\u0000~\u0000ysr\u0000\"com.sun.msv.grammar.ExpressionPool\u0000\u0000\u0000"
+"\u0000\u0000\u0000\u0000\u0001\u0002\u0000\u0001L\u0000\bexpTablet\u0000/Lcom/sun/msv/grammar/ExpressionPool$Cl"
+"osedHash;xpsr\u0000-com.sun.msv.grammar.ExpressionPool$ClosedHash"
+"\u00d7j\u00d0N\u00ef\u00e8\u00ed\u001c\u0003\u0000\u0003I\u0000\u0005countB\u0000\rstreamVersionL\u0000\u0006parentt\u0000$Lcom/sun/msv/"
+"grammar/ExpressionPool;xp\u0000\u0000\u0000\u001c\u0001pq\u0000~\u0000\u0007q\u0000~\u0000[q\u0000~\u0000\u0005q\u0000~\u0000%q\u0000~\u0000Pq\u0000~\u0000"
+"eq\u0000~\u0000\bq\u0000~\u0000\u000fq\u0000~\u0000\'q\u0000~\u0000Jq\u0000~\u0000Rq\u0000~\u0000_q\u0000~\u0000gq\u0000~\u0000\u0012q\u0000~\u0000(q\u0000~\u0000Kq\u0000~\u0000Sq\u0000~\u0000"
+"`q\u0000~\u0000hq\u0000~\u0000\\q\u0000~\u0000\u0006q\u0000~\u0000,q\u0000~\u0000Wq\u0000~\u0000lq\u0000~\u0000Gq\u0000~\u0000\nq\u0000~\u0000Hq\u0000~\u0000]x"));
        }
        return new com.sun.msv.verifier.regexp.REDocumentDeclaration(schemaFragment);
    }

    public class Unmarshaller
        extends com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.AbstractUnmarshallingEventHandlerImpl
    {


        public Unmarshaller(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.UnmarshallingContext context) {
            super(context, "--------------");
        }

        protected Unmarshaller(com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.runtime.UnmarshallingContext context, int startState) {
            this(context);
            state = startState;
        }

        public java.lang.Object owner() {
            return com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this;
        }

        public void enterElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname, org.xml.sax.Attributes __atts)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        attIdx = context.getAttribute("", "AuthenticationMethod");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 6;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  7 :
                        if (("SubjectLocality" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityImpl) spawnChildFromEnterElement((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityImpl.class), 10, ___uri, ___local, ___qname, __atts));
                            return ;
                        }
                        if (("SubjectLocality" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 8;
                            return ;
                        }
                        state = 10;
                        continue outer;
                    case  6 :
                        if (("Subject" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            spawnHandlerFromEnterElement((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        if (("Subject" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            spawnHandlerFromEnterElement((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        spawnHandlerFromEnterElement((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname, __atts);
                        return ;
                    case  8 :
                        attIdx = context.getAttribute("", "DNSAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        attIdx = context.getAttribute("", "IPAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromEnterElement((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname, __atts));
                        return ;
                    case  11 :
                        attIdx = context.getAttribute("", "AuthorityKind");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().enterElement(___uri, ___local, ___qname, __atts);
                            return ;
                        }
                        break;
                    case  10 :
                        if (("AuthorityBinding" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            _getAuthorityBinding().add(((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingImpl) spawnChildFromEnterElement((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingImpl.class), 13, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("AuthorityBinding" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 11;
                            return ;
                        }
                        state = 13;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "AuthenticationInstant");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  13 :
                        if (("AuthorityBinding" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            _getAuthorityBinding().add(((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingImpl) spawnChildFromEnterElement((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingImpl.class), 13, ___uri, ___local, ___qname, __atts)));
                            return ;
                        }
                        if (("AuthorityBinding" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            context.pushAttributes(__atts, false);
                            state = 11;
                            return ;
                        }
                        revertToParentFromEnterElement(___uri, ___local, ___qname, __atts);
                        return ;
                }
                super.enterElement(___uri, ___local, ___qname, __atts);
                break;
            }
        }

        private void eatText1(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _AuthenticationMethod = com.sun.xml.bind.WhiteSpaceProcessor.collapse(value);
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        private void eatText2(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            try {
                _AuthenticationInstant = ((java.util.Calendar) com.sun.msv.datatype.xsd.DateTimeType.theInstance.createJavaObject(com.sun.xml.bind.WhiteSpaceProcessor.collapse(value), null));
            } catch (java.lang.Exception e) {
                handleParseConversionException(e);
            }
        }

        public void leaveElement(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        attIdx = context.getAttribute("", "AuthenticationMethod");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 6;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  7 :
                        state = 10;
                        continue outer;
                    case  6 :
                        spawnHandlerFromLeaveElement((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname);
                        return ;
                    case  8 :
                        attIdx = context.getAttribute("", "DNSAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        attIdx = context.getAttribute("", "IPAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromLeaveElement((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname));
                        return ;
                    case  9 :
                        if (("SubjectLocality" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            context.popAttributes();
                            state = 10;
                            return ;
                        }
                        break;
                    case  11 :
                        attIdx = context.getAttribute("", "AuthorityKind");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveElement(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  10 :
                        state = 13;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "AuthenticationInstant");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  12 :
                        if (("AuthorityBinding" == ___local)&&("urn:oasis:names:tc:SAML:1.0:assertion" == ___uri)) {
                            context.popAttributes();
                            state = 13;
                            return ;
                        }
                        break;
                    case  13 :
                        revertToParentFromLeaveElement(___uri, ___local, ___qname);
                        return ;
                }
                super.leaveElement(___uri, ___local, ___qname);
                break;
            }
        }

        public void enterAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        if (("AuthenticationMethod" == ___local)&&("" == ___uri)) {
                            state = 4;
                            return ;
                        }
                        break;
                    case  7 :
                        state = 10;
                        continue outer;
                    case  6 :
                        spawnHandlerFromEnterAttribute((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname);
                        return ;
                    case  8 :
                        if (("DNSAddress" == ___local)&&("" == ___uri)) {
                            _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromEnterAttribute((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname));
                            return ;
                        }
                        if (("IPAddress" == ___local)&&("" == ___uri)) {
                            _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromEnterAttribute((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname));
                            return ;
                        }
                        _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromEnterAttribute((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname));
                        return ;
                    case  11 :
                        if (("AuthorityKind" == ___local)&&("" == ___uri)) {
                            _getAuthorityBinding().add(((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingTypeImpl) spawnChildFromEnterAttribute((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthorityBindingTypeImpl.class), 12, ___uri, ___local, ___qname)));
                            return ;
                        }
                        break;
                    case  10 :
                        state = 13;
                        continue outer;
                    case  0 :
                        if (("AuthenticationInstant" == ___local)&&("" == ___uri)) {
                            state = 1;
                            return ;
                        }
                        break;
                    case  13 :
                        revertToParentFromEnterAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.enterAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void leaveAttribute(java.lang.String ___uri, java.lang.String ___local, java.lang.String ___qname)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                switch (state) {
                    case  3 :
                        attIdx = context.getAttribute("", "AuthenticationMethod");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 6;
                            eatText1(v);
                            continue outer;
                        }
                        break;
                    case  7 :
                        state = 10;
                        continue outer;
                    case  5 :
                        if (("AuthenticationMethod" == ___local)&&("" == ___uri)) {
                            state = 6;
                            return ;
                        }
                        break;
                    case  2 :
                        if (("AuthenticationInstant" == ___local)&&("" == ___uri)) {
                            state = 3;
                            return ;
                        }
                        break;
                    case  6 :
                        spawnHandlerFromLeaveAttribute((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, ___uri, ___local, ___qname);
                        return ;
                    case  8 :
                        attIdx = context.getAttribute("", "DNSAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        attIdx = context.getAttribute("", "IPAddress");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromLeaveAttribute((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, ___uri, ___local, ___qname));
                        return ;
                    case  11 :
                        attIdx = context.getAttribute("", "AuthorityKind");
                        if (attIdx >= 0) {
                            context.consumeAttribute(attIdx);
                            context.getCurrentHandler().leaveAttribute(___uri, ___local, ___qname);
                            return ;
                        }
                        break;
                    case  10 :
                        state = 13;
                        continue outer;
                    case  0 :
                        attIdx = context.getAttribute("", "AuthenticationInstant");
                        if (attIdx >= 0) {
                            final java.lang.String v = context.eatAttribute(attIdx);
                            state = 3;
                            eatText2(v);
                            continue outer;
                        }
                        break;
                    case  13 :
                        revertToParentFromLeaveAttribute(___uri, ___local, ___qname);
                        return ;
                }
                super.leaveAttribute(___uri, ___local, ___qname);
                break;
            }
        }

        public void handleText(final java.lang.String value)
            throws org.xml.sax.SAXException
        {
            int attIdx;
            outer:
            while (true) {
                try {
                    switch (state) {
                        case  3 :
                            attIdx = context.getAttribute("", "AuthenticationMethod");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 6;
                                eatText1(v);
                                continue outer;
                            }
                            break;
                        case  7 :
                            state = 10;
                            continue outer;
                        case  1 :
                            state = 2;
                            eatText2(value);
                            return ;
                        case  6 :
                            spawnHandlerFromText((((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectStatementAbstractTypeImpl)com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.AuthenticationStatementTypeImpl.this).new Unmarshaller(context)), 7, value);
                            return ;
                        case  8 :
                            attIdx = context.getAttribute("", "DNSAddress");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            attIdx = context.getAttribute("", "IPAddress");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            _SubjectLocality = ((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl) spawnChildFromText((com.sun.xml.wss.saml.internal.saml11.jaxb10.impl.SubjectLocalityTypeImpl.class), 9, value));
                            return ;
                        case  11 :
                            attIdx = context.getAttribute("", "AuthorityKind");
                            if (attIdx >= 0) {
                                context.consumeAttribute(attIdx);
                                context.getCurrentHandler().text(value);
                                return ;
                            }
                            break;
                        case  10 :
                            state = 13;
                            continue outer;
                        case  0 :
                            attIdx = context.getAttribute("", "AuthenticationInstant");
                            if (attIdx >= 0) {
                                final java.lang.String v = context.eatAttribute(attIdx);
                                state = 3;
                                eatText2(v);
                                continue outer;
                            }
                            break;
                        case  4 :
                            state = 5;
                            eatText1(value);
                            return ;
                        case  13 :
                            revertToParentFromText(value);
                            return ;
                    }
                } catch (java.lang.RuntimeException e) {
                    handleUnexpectedTextException(value, e);
                }
                break;
            }
        }

    }

}
