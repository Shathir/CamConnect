package com.outdu.camconnect.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.outdu.camconnect.R
import com.outdu.camconnect.ui.models.CountryCode
import com.outdu.camconnect.ui.theme.AppColors.StravionBlue
import com.outdu.camconnect.ui.theme.camConnectIsDarkTheme

@Composable
fun SearchableCountryDropdown(
    selectedCountryCode: String,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isDarkTheme = camConnectIsDarkTheme()
    
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            CountryCode.VALID_CODES
        } else {
            CountryCode.VALID_CODES.filter {
                it.code.contains(searchQuery, ignoreCase = true) ||
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val selectedCountry = CountryCode.fromCode(selectedCountryCode)
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = selectedCountry?.displayName ?: selectedCountryCode,
            onValueChange = {},
            readOnly = true,
            label = { 
                Text(
                    text = "WiFi Country Code",
                    fontFamily = FontFamily(Font(R.font.arial_regular))
                ) 
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = StravionBlue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (isDarkTheme) Color.White else Color.Black,
                disabledBorderColor = if (isDarkTheme) Color.Gray else Color.LightGray,
                disabledLabelColor = if (isDarkTheme) Color.Gray else Color.DarkGray,
                disabledTrailingIconColor = StravionBlue
            ),
            textStyle = TextStyle(
                fontFamily = FontFamily(Font(R.font.arial_regular)),
                fontSize = 16.sp
            )
        )
        
        if (expanded) {
            Dialog(onDismissRequest = { 
                expanded = false
                searchQuery = ""
            }) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Country",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.arial_regular)),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text(
                                    text = "Search country...",
                                    fontFamily = FontFamily(Font(R.font.arial_regular))
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Search, 
                                    contentDescription = "Search",
                                    tint = StravionBlue
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StravionBlue,
                                focusedLabelColor = StravionBlue,
                                cursorColor = StravionBlue
                            ),
                            textStyle = TextStyle(
                                fontFamily = FontFamily(Font(R.font.arial_regular)),
                                fontSize = 16.sp
                            )
                        )
                        
                        if (filteredCountries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No countries found",
                                    fontFamily = FontFamily(Font(R.font.arial_regular)),
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(filteredCountries) { country ->
                                    CountryItem(
                                        country = country,
                                        isSelected = country.code == selectedCountryCode,
                                        onClick = {
                                            onCountrySelected(country.code)
                                            expanded = false
                                            searchQuery = ""
                                        },
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryItem(
    country: CountryCode,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isSelected -> StravionBlue
                    isDarkTheme -> Color(0xFF2A2A2A)
                    else -> Color(0xFFF5F5F5)
                }
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = country.displayName,
            fontFamily = FontFamily(Font(R.font.arial_regular)),
            fontSize = 16.sp,
            color = when {
                isSelected -> Color.White
                isDarkTheme -> Color.White
                else -> Color.Black
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
