package com.example.grow.ui.components

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.grow.R
import com.example.grow.ui.theme.BiruMudaMain
import com.example.grow.ui.theme.BiruPrimer
import com.example.grow.ui.theme.BiruText
import com.example.grow.ui.theme.GROWTheme
import com.example.grow.ui.theme.PoppinsFamily
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            modifier = Modifier.clickable { onBackClick() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ProfilePicture(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(BiruMudaMain),
        contentAlignment = Alignment.Center
    ) {
        // Placeholder avatar image
        Image(
            painter = painterResource(id = R.drawable.ic_star),
            contentDescription = "Profile Picture",
            modifier = Modifier.size(80.dp)
        )

        // Edit button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(BiruPrimer)
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile Picture",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BiruPrimer),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .background(BiruMudaMain, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                containerColor = BiruMudaMain
            )
        )
    }
}

@Composable
fun ProfileUpdateScreen(navController: NavController) {
    var nameValue by remember { mutableStateOf("") }
    var emailValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        TopBar(title = "Perbarui Profil") {
            navController.navigateUp()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            ProfilePicture()

            Spacer(modifier = Modifier.height(48.dp))

            FormTextField(
                label = "Nama",
                placeholder = "Nama Lengkap",
                value = nameValue,
                onValueChange = { nameValue = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FormTextField(
                label = "Email",
                placeholder = "mail@mail.com",
                value = emailValue,
                onValueChange = { emailValue = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Perbarui",
                onClick = {
                    // Handle update profile
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
fun GenderSelectionRow(
    selectedGender: String?,
    onGenderSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Jenis Kelamin",
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GenderOption(
            gender = "Laki - Laki",
            isSelected = selectedGender == "Laki - Laki",
            onSelect = { onGenderSelected("Laki - Laki") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        GenderOption(
            gender = "Perempuan",
            isSelected = selectedGender == "Perempuan",
            onSelect = { onGenderSelected("Perempuan") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun GenderOption(
    gender: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = BiruPrimer,
                unselectedColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = gender,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ChildDataUpdateScreen(navController: NavController) {
    var nameValue by remember { mutableStateOf("") }
    var birthDateValue by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        TopBar(title = "Perbarui Data Anak") {
            navController.navigateUp()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            ProfilePicture()

            Spacer(modifier = Modifier.height(48.dp))

            FormTextField(
                label = "Nama Lengkap",
                placeholder = "Nama Lengkap",
                value = nameValue,
                onValueChange = { nameValue = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            FormTextField(
                label = "Tanggal Lahir",
                placeholder = "DD/MM/YYYY",
                value = birthDateValue,
                onValueChange = { birthDateValue = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            GenderSelectionRow(
                selectedGender = selectedGender,
                onGenderSelected = { selectedGender = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            PrimaryButton(
                text = "Perbarui",
                onClick = {
                    // Handle update child data
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
fun ChildDataCard(
    name: String,
    age: String,
    nutritionStatus: String,
    stuntingStatus: String,
    date: String,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Child Avatar
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(BiruMudaMain),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Child Avatar",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = BiruText
                    )

                    Text(
                        text = age,
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                TextButton(onClick = onEditClick) {
                    Text(
                        text = "Ubah Data",
                        fontFamily = PoppinsFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = BiruText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Nutrition Status
                StatusItem(
                    title = "Status Gizi",
                    status = nutritionStatus,
                    date = date,
                    modifier = Modifier.weight(1f)
                )

                // Stunting Status
                StatusItem(
                    title = "Status Stunting",
                    status = stuntingStatus,
                    date = date,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatusItem(
    title: String,
    status: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View Detail",
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = status,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF7ED957))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = date,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ChildDataListScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        TopBar(title = "Data Anak") {
            // Handle back navigation if needed
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sample data
        ChildDataCard(
            name = "Wulan bin Fulan",
            age = "3 Tahun 2 Bulan",
            nutritionStatus = "Normal",
            stuntingStatus = "Normal",
            date = "11 Mar 2022",
            onEditClick = {
                navController.navigate("childDataUpdate")
            }
        )

        ChildDataCard(
            name = "Wulan bin Fulan",
            age = "3 Tahun 2 Bulan",
            nutritionStatus = "Normal",
            stuntingStatus = "Normal",
            date = "11 Mar 2022",
            onEditClick = {
                navController.navigate("childDataUpdate")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        PrimaryButton(
            text = "Tambah Data Anak",
            onClick = {
                navController.navigate("childDataUpdate")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileUpdatePreview() {
    GROWTheme {
        ProfileUpdateScreen(rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ChildDataUpdatePreview() {
    GROWTheme {
        ChildDataUpdateScreen(rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ChildDataListPreview() {
    GROWTheme {
        ChildDataListScreen(rememberNavController())
    }
}