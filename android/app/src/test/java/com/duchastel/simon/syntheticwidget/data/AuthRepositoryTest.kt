package com.duchastel.simon.syntheticwidget.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun `getMaskedApiKey returns empty string when no api key saved`() = runBlocking {
        val repository = AuthRepositoryImpl()
        
        val result = repository.getMaskedApiKey().first()
        
        assertEquals("", result)
    }

    @Test
    fun `saveApiKey does not throw exception`() = runBlocking {
        val repository = AuthRepositoryImpl()
        
        repository.saveApiKey("syn_test_api_key_1234")
        
        // No exception should be thrown
    }
}

class ApiKeyMaskerTest {

    @Test
    fun `mask returns empty string for null input`() {
        assertEquals("", ApiKeyMasker.mask(null))
    }

    @Test
    fun `mask returns empty string for empty input`() {
        assertEquals("", ApiKeyMasker.mask(""))
    }

    @Test
    fun `mask returns all stars for key without syn_ prefix`() {
        val apiKey = "some_other_key_1234"
        val expected = "******************"
        
        assertEquals(expected, ApiKeyMasker.mask(apiKey))
    }

    @Test
    fun `mask returns syn_ prefix with stars and last 4 digits for valid key`() {
        val apiKey = "syn_abcdefghijklmnop1234"
        val expected = "syn_****************1234"
        
        assertEquals(expected, ApiKeyMasker.mask(apiKey))
    }

    @Test
    fun `mask returns original key when shorter than prefix plus 4 digits`() {
        val apiKey = "syn_1234"
        
        assertEquals("syn_1234", ApiKeyMasker.mask(apiKey))
    }

    @Test
    fun `mask returns original key when exactly prefix plus 4 digits`() {
        val apiKey = "syn_abcd1234"
        
        assertEquals("syn_abcd1234", ApiKeyMasker.mask(apiKey))
    }

    @Test
    fun `mask correctly handles long api key`() {
        val apiKey = "syn_very_long_api_key_with_many_characters_1234"
        val expected = "syn_***************************************1234"
        
        assertEquals(expected, ApiKeyMasker.mask(apiKey))
    }
}
