<?xml version="1.0" encoding="UTF-8" ?>
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" version="3.0" >
    <xsl:output method="xml" encoding="UTF-8" indent="yes" />

    <xsl:template match="/ArrayList">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="page"
                page-height="297mm" page-width="210mm"
                margin-top="20mm" margin-bottom="10mm"
                margin-left="25mm" margin-right="25mm">
                <fo:region-body
                    margin-top="0mm" margin-bottom="15mm"
                    margin-left="0mm" margin-right="0mm"/>
                <fo:region-after extent="10mm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="page">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block>fubar</fo:block>>
                    <xsl:apply-templates/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="header1">     
        <fo:block>      
            <xsl:number level="multiple" count="header1" format="1 "/>  
            <xsl:value-of select="title"/> 
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="sub">
        <fo:block>      
            <xsl:number level="multiple" count="sub|header1" format="1. "/>  
            <xsl:value-of select="concat('Head - ',title)"/>      
        </fo:block>
    </xsl:template>

    <xsl:template match="text()"/>
</xsl:transform>
