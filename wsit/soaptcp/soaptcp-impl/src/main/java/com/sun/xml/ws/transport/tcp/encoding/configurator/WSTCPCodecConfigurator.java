/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.transport.tcp.encoding.configurator;

/**
 * SOAP/TCP code configurator
 * 
 * @author Alexey Stashok
 */
public enum WSTCPCodecConfigurator {
    INSTANCE;

    private static final int MIN_INDEXED_STRING_SIZE_LIMIT = 0;
    private static final int MAX_INDEXED_STRING_SIZE_LIMIT = 32;
    private static final int INDEXED_STRING_MEMORY_LIMIT = 4 * 1024 * 1024; //4M limit
    
    
    private DocumentParserFactory documentParserFactory = new DefaultDocumentParserFactory();
    private DocumentSerializerFactory documentSerializerFactory = new DefaultDocumentSerializerFactory();
    
    private ParserVocabularyFactory parserVocabularyFactory = new DefaultParserVocabularyFactory();
    private SerializerVocabularyFactory serializerVocabularyFactory = new DefaultSerializerVocabularyFactory();
    
    private int minAttributeValueSize = MIN_INDEXED_STRING_SIZE_LIMIT;
    private int maxAttributeValueSize = MAX_INDEXED_STRING_SIZE_LIMIT;
    private int minCharacterContentChunkSize = MIN_INDEXED_STRING_SIZE_LIMIT;
    private int maxCharacterContentChunkSize = MAX_INDEXED_STRING_SIZE_LIMIT;
    private int attributeValueMapMemoryLimit = INDEXED_STRING_MEMORY_LIMIT;
    private int characterContentChunkMapMemoryLimit = INDEXED_STRING_MEMORY_LIMIT;
    
    /**
     * Get the {@link DocumentParserFactory}
     * @return {@link DocumentParserFactory}
     */
    public DocumentParserFactory getDocumentParserFactory() {
        return documentParserFactory;
    }

    /**
     * Set the {@link DocumentParserFactory}
     * @param {@link DocumentParserFactory}
     */
    public void setDocumentParserFactory(DocumentParserFactory documentParserFactory) {
        this.documentParserFactory = documentParserFactory;
    }

    /**
     * Get the {@link DocumentSerializerFactory}
     * @return {@link DocumentSerializerFactory}
     */
    public DocumentSerializerFactory getDocumentSerializerFactory() {
        return documentSerializerFactory;
    }

    /**
     * Set the {@link DocumentSerializerFactory}
     * @param {@link DocumentSerializerFactory}
     */
    public void setDocumentSerializerFactory(DocumentSerializerFactory documentSerializerFactory) {
        this.documentSerializerFactory = documentSerializerFactory;
    }

    /**
     * Get the {@link ParserVocabularyFactory}
     * @return {@link ParserVocabularyFactory}
     */
    public ParserVocabularyFactory getParserVocabularyFactory() {
        return parserVocabularyFactory;
    }

    /**
     * Set the {@link ParserVocabularyFactory}
     * @param {@link ParserVocabularyFactory}
     */
    public void setParserVocabularyFactory(ParserVocabularyFactory parserVocabularyFactory) {
        this.parserVocabularyFactory = parserVocabularyFactory;
    }

    /**
     * Get the {@link SerializerVocabularyFactory}
     * @return {@link SerializerVocabularyFactory}
     */
    public SerializerVocabularyFactory getSerializerVocabularyFactory() {
        return serializerVocabularyFactory;
    }

    /**
     * Set the {@link SerializerVocabularyFactory}
     * @param {@link SerializerVocabularyFactory}
     */
    public void setSerializerVocabularyFactory(SerializerVocabularyFactory serializerVocabularyFactory) {
        this.serializerVocabularyFactory = serializerVocabularyFactory;
    }


    /**
     * Gets the minimum size of attribute values
     * that will be indexed.
     *
     * @return The minimum attribute values size.
     */
    public int getMinAttributeValueSize() {
        return minAttributeValueSize;
    }

    /**
     * Sets the minimum size of attribute values
     * that will be indexed.
     *
     * @param size the minimum attribute values size.
     */
    public void setMinAttributeValueSize(int minAttributeValueSize) {
        this.minAttributeValueSize = minAttributeValueSize;
    }

    /**
     * Gets the maximum size of attribute values
     * that will be indexed.
     *
     * @return The maximum attribute values size.
     */
    public int getMaxAttributeValueSize() {
        return maxAttributeValueSize;
    }

    /**
     * Sets the maximum size of attribute values
     * that will be indexed.
     *
     * @param size the maximum attribute values size.
     */
    public void setMaxAttributeValueSize(int maxAttributeValueSize) {
        this.maxAttributeValueSize = maxAttributeValueSize;
    }

    /**
     * Gets the limit on the memory size of Map of attribute values
     * that will be indexed.
     *
     * @return The attribute value size limit.
     */
    public int getAttributeValueMapMemoryLimit() {
        return attributeValueMapMemoryLimit;
    }

    /**
     * Sets the limit on the memory size of Map of attribute values
     * that will be indexed.
     *
     * @param size The attribute value size limit. Any value less
     * that a length of size limit will be indexed.
     */
    public void setAttributeValueMapMemoryLimit(int attributeValueMapMemoryLimit) {
        this.attributeValueMapMemoryLimit = attributeValueMapMemoryLimit;
    }

    /**
     * Gets the minimum size of character content chunks
     * that will be indexed.
     *
     * @return The minimum character content chunk size.
     */
    public int getMinCharacterContentChunkSize() {
        return minCharacterContentChunkSize;
    }

    /**
     * Sets the minimum size of character content chunks
     * that will be indexed.
     *
     * @param size the minimum character content chunk size.
     */
    public void setMinCharacterContentChunkSize(int minCharacterContentChunkSize) {
        this.minCharacterContentChunkSize = minCharacterContentChunkSize;
    }

    /**
     * Gets the maximum size of character content chunks
     * that will be indexed.
     *
     * @return The maximum character content chunk size.
     */
    public int getMaxCharacterContentChunkSize() {
        return maxCharacterContentChunkSize;
    }

    /**
     * Sets the maximum size of character content chunks
     * that will be indexed.
     *
     * @param size the maximum character content chunk size.
     */
    public void setMaxCharacterContentChunkSize(int maxCharacterContentChunkSize) {
        this.maxCharacterContentChunkSize = maxCharacterContentChunkSize;
    }

    /**
     * Gets the limit on the memory size of Map of attribute values
     * that will be indexed.
     *
     * @return The attribute value size limit.
     */
    public int getCharacterContentChunkMapMemoryLimit() {
        return characterContentChunkMapMemoryLimit;
    }

    /**
     * Sets the limit on the memory size of Map of attribute values
     * that will be indexed.
     *
     * @param size The attribute value size limit. Any value less
     * that a length of size limit will be indexed.
     */
    public void setCharacterContentChunkMapMemoryLimit(int characterContentChunkMapMemoryLimit) {
        this.characterContentChunkMapMemoryLimit = characterContentChunkMapMemoryLimit;
    }
}
