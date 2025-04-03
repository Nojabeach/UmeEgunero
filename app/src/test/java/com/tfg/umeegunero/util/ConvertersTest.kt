package com.tfg.umeegunero.util

import com.tfg.umeegunero.data.model.AttachmentType
import com.tfg.umeegunero.data.model.InteractionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class ConvertersTest {
    
    private val converters = Converters()
    
    @Test
    fun `test date timestamp conversion`() {
        val now = Date()
        val timestamp = converters.dateToTimestamp(now)
        val convertedDate = converters.fromTimestamp(timestamp)
        
        assertEquals(now.time, convertedDate?.time)
    }
    
    @Test
    fun `test string list conversion`() {
        val list = listOf("one", "two", "three")
        val json = converters.fromStringList(list)
        val convertedList = converters.toStringList(json)
        
        assertEquals(list, convertedList)
    }
    
    @Test
    fun `test empty string list conversion`() {
        val list = emptyList<String>()
        val json = converters.fromStringList(list)
        val convertedList = converters.toStringList(json)
        
        assertEquals(list, convertedList)
    }
    
    @Test
    fun `test string map conversion`() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val json = converters.fromStringMap(map)
        val convertedMap = converters.toStringMap(json)
        
        assertEquals(map, convertedMap)
    }
    
    @Test
    fun `test empty string map conversion`() {
        val map = emptyMap<String, String>()
        val json = converters.fromStringMap(map)
        val convertedMap = converters.toStringMap(json)
        
        assertEquals(map, convertedMap)
    }
    
    @Test
    fun `test interaction status conversion`() {
        val status = InteractionStatus.READING
        val statusString = converters.fromInteractionStatus(status)
        val convertedStatus = converters.toInteractionStatus(statusString)
        
        assertEquals(status, convertedStatus)
    }
    
    @Test
    fun `test null interaction status conversion`() {
        val statusString = converters.fromInteractionStatus(null)
        val convertedStatus = converters.toInteractionStatus(statusString)
        
        assertEquals(InteractionStatus.NONE, convertedStatus)
    }
    
    @Test
    fun `test invalid interaction status conversion`() {
        val convertedStatus = converters.toInteractionStatus("INVALID_STATUS")
        
        assertEquals(InteractionStatus.NONE, convertedStatus)
    }
    
    @Test
    fun `test attachment type conversion`() {
        val attachmentType = AttachmentType.IMAGE
        val typeString = converters.fromAttachmentType(attachmentType)
        val convertedType = converters.toAttachmentType(typeString)
        
        assertEquals(attachmentType, convertedType)
    }
    
    @Test
    fun `test null attachment type conversion`() {
        val typeString = converters.fromAttachmentType(null)
        val convertedType = converters.toAttachmentType(typeString)
        
        assertNull(convertedType)
    }
    
    @Test
    fun `test invalid attachment type conversion`() {
        val convertedType = converters.toAttachmentType("INVALID_TYPE")
        
        assertNull(convertedType)
    }
} 