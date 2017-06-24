<?xml version="1.0" encoding="utf-8"?>
<!--

    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

    Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.

    The contents of this file are subject to the terms of either the GNU
    General Public License Version 2 only ("GPL") or the Common Development
    and Distribution License("CDDL") (collectively, the "License").  You
    may not use this file except in compliance with the License.  You can
    obtain a copy of the License at
    https://oss.oracle.com/licenses/CDDL+GPL-1.1
    or LICENSE.txt.  See the License for the specific
    language governing permissions and limitations under the License.

    When distributing the software, include this License Header Notice in each
    file and include the License file at LICENSE.txt.

    GPL Classpath Exception:
    Oracle designates this particular file as subject to the "Classpath"
    exception as provided by Oracle in the GPL Version 2 section of the License
    file that accompanied this code.

    Modifications:
    If applicable, add the following below the License Header, with the fields
    enclosed by brackets [] replaced by your own identifying information:
    "Portions Copyright [year] [name of copyright owner]"

    Contributor(s):
    If you wish your version of this file to be governed by only the CDDL or
    only the GPL Version 2, indicate your decision by adding "[Contributor]
    elects to include this software in this distribution under the [CDDL or GPL
    Version 2] license."  If you don't indicate a single choice of license, a
    recipient has the option to distribute your version of this file under
    either the CDDL, the GPL Version 2 or to extend the choice of license to
    its licensees as provided above.  However, if you add GPL Version 2 code
    and therefore, elected the GPL Version 2 license, then the option applies
    only if the new code is made subject to such option by the copyright
    holder.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:d="http://docbook.org/ns/docbook"
                exclude-result-prefixes="fo d"
                version="1.0">

    <xsl:import href="common.xsl"/>
    <xsl:import href="htmlProcessing.xsl"/>
    <xsl:import href="urn:docbkx:stylesheet/autotoc.xsl"/>

    <xsl:template match="d:itemizedlist[@role = 'package']" mode="class.value">
        <xsl:value-of select="'itemizedlist package'"/>
    </xsl:template>

    <xsl:template match="d:itemizedlist[@role = 'document']" mode="class.value">
        <xsl:value-of select="'itemizedlist document'"/>
    </xsl:template>

    <xsl:param name="css.decoration" select="0" />

    <!--Override ToC lines-->
    <xsl:template name="toc.line">
        <xsl:param name="toc-context" select="."/>
        <xsl:param name="depth" select="1"/>
        <xsl:param name="depth.from.context" select="8"/>

        <div>
            <xsl:attribute name="class"><xsl:value-of select="local-name(.)"/></xsl:attribute>

            <!-- * if $autotoc.label.in.hyperlink is zero, then output the label -->
            <!-- * before the hyperlinked title (as the DSSSL stylesheet does) -->
            <xsl:if test="$autotoc.label.in.hyperlink = 0">
                <xsl:variable name="label">
                    <xsl:apply-templates select="." mode="label.markup"/>
                </xsl:variable>
                <xsl:copy-of select="$label"/>
                <xsl:if test="$label != ''">
                    <xsl:value-of select="$autotoc.label.separator"/>
                </xsl:if>
            </xsl:if>

            <a>
                <xsl:attribute name="href">
                    <xsl:call-template name="href.target">
                        <xsl:with-param name="context" select="$toc-context"/>
                        <xsl:with-param name="toc-context" select="$toc-context"/>
                    </xsl:call-template>
                </xsl:attribute>

                <xsl:if test="local-name(.) = 'article'">
                    <img src="icons/book.gif" class="article-image" />
                </xsl:if>

                <!-- * if $autotoc.label.in.hyperlink is non-zero, then output the label -->
                <!-- * as part of the hyperlinked title -->
                <xsl:if test="not($autotoc.label.in.hyperlink = 0)">
                    <xsl:variable name="label">
                        <xsl:apply-templates select="." mode="label.markup"/>
                    </xsl:variable>
                    <xsl:copy-of select="$label"/>
                    <xsl:if test="$label != ''">
                        <xsl:value-of select="$autotoc.label.separator"/>
                    </xsl:if>
                </xsl:if>

                <xsl:apply-templates select="." mode="titleabbrev.markup"/>
            </a>

            <xsl:if test="local-name(.) = 'article' and ./d:info/d:abstract/d:para">
                <br /><br />
                <xsl:value-of select="./d:info/d:abstract/d:para" />
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template name="user.header.content">
        <small class="small">Links: <a href="index.html">Table of Contents</a> | <a href="getting-started.html">Single HTML</a> | <a
                href="getting-started.pdf">Single PDF</a></small>
    </xsl:template>

    <xsl:template name="user.head.content">
<script type="text/javascript">
  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-2105126-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
    </xsl:template>

</xsl:stylesheet>
