package com.unibague.pocketpal

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.unibague.pocketpal.ui.theme.PocketPalTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.CollectionReference

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        auth = FirebaseAuth.getInstance()
        setContent {
            PocketPalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigations(auth)
                }
            }
        }
    }
}

@Composable
fun AppNavigations(auth: FirebaseAuth) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("login") { LoginScreen(navController, auth) }
        composable("register") { RegisterScreen(navController, auth) }
        composable("home") { HomeScreen(navController) }
        composable("splash") { SplashScreen(navController) }
        composable("tasks") { TaskScreen(navController) }
    }
}


@Composable
fun LoginScreen(navController: NavHostController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "PocketPal Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
        )
        Text(text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                signInWithFirebase(auth, email, password) { success, message ->
                    loginState = if (success) {
                        navController.navigate("home")
                        "¡Datos correctos!"
                    } else (@Composable {
                        Toast.makeText(
                            LocalContext.current,
                            "Error: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                    }).toString()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Iniciar Sesión")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Crear cuenta", color = MaterialTheme.colorScheme.onBackground)
        }

        loginState?.let {
            Text(
                text = it,
                color = if (it.startsWith("¡Datos correctos!")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

fun signInWithFirebase(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onResult: (Boolean, String?) -> Unit
) {
    if (email.isEmpty() || password.isEmpty()) {
        onResult(false, "Los campos no pueden estar vacíos")
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onResult(true, null)
            } else {
                onResult(false, task.exception?.message)
            }
        }
}

@Composable
fun RegisterScreen(navController: NavHostController, auth: FirebaseAuth) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerState by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "PocketPal Logo",
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
        )
        Text(text = "Registro",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            registerState = "¡Usuario creado con éxito!"
                            navController.popBackStack()
                        } else {
                            registerState = "Error: ${task.exception?.message}"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrarse")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver a Iniciar Sesión", color = MaterialTheme.colorScheme.onBackground)
        }

        registerState?.let {
            Text(
                text = it,
                color = if (it.startsWith("¡Usuario creado")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun HomeScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val tasksCollection: CollectionReference = db.collection("users").document(userId).collection("tasks")
    val recentTasks = remember { mutableStateListOf<Task>() }

    // Cargar tareas recientes
    LaunchedEffect(Unit) {
        tasksCollection.limit(3).get().addOnSuccessListener { result ->
            recentTasks.clear()
            for (document in result) {
                val task = document.toObject(Task::class.java)
                recentTasks.add(task)
            }
        }
    }

    Scaffold(
        topBar = { HomeTopBar(navController) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Nombre de la Mascota",
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Image(
                            painter = painterResource(R.drawable.pet),
                            contentDescription = "Imagen de la mascota",
                            modifier = Modifier.size(250.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                        .clickable {
                            navController.navigate("tasks")
                        }
                ) {
                    Text(
                        text = "Tareas Recientes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(recentTasks) { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (task.completed) {
                                    Icon(
                                        painter = painterResource(R.drawable.check_circle),
                                        contentDescription = "Completada",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        if (recentTasks.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay tareas recientes",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(navController: NavHostController) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = "Pocket Pal",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    painter = painterResource(R.drawable.account),
                    contentDescription = "Menú"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Cerrar sesión") },
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        expanded = false
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Tareas", "tasks", painterResource(R.drawable.list)),
        BottomNavItem("Home", "home", painterResource(R.drawable.home) ),
        BottomNavItem("Pomodoro", "pomodoro", painterResource(R.drawable.timer))
    )
    NavigationBar {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination == item.route,
                onClick = { navController.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
fun SplashScreen(navController: NavHostController) {
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        if (currentUser != null) {
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Cargando...", style = MaterialTheme.typography.titleLarge)
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: Painter
)

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val completed: Boolean = false
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()
    val tasksCollection: CollectionReference = db.collection("users").document(userId).collection("tasks")
    val tasks = remember { mutableStateListOf<Task>() }
    var showDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(true) {
        tasksCollection.get().addOnSuccessListener { result ->
            val newTasks = mutableListOf<Task>()
            for (document in result) {
                val task = document.toObject(Task::class.java)
                if (!tasks.any { it.id == task.id }) {
                    newTasks.add(task)
                }
            }
            tasks.clear()
            tasks.addAll(newTasks)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareas") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            painterResource(R.drawable.add),
                            contentDescription = "Agregar tarea"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        content = { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onTaskUpdated = {
                                taskToEdit = it
                                showDialog = true
                            },
                            onStateChanged = { updatedTask ->
                                tasksCollection.document(updatedTask.id)
                                    .update("completed", updatedTask.completed)
                                    .addOnSuccessListener {
                                        val index = tasks.indexOfFirst { it.id == updatedTask.id }
                                        if (index != -1) {
                                            tasks[index] = updatedTask
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("TaskScreen", "Error al actualizar el estado de la tarea: ${exception.message}")
                                    }
                            }
                        )
                    }
                }
            }

            if (showDialog && taskToEdit == null) {
                AddTaskDialog(
                    onDismiss = { showDialog = false },
                    onAddTask = { newTask ->
                        tasksCollection.document(newTask.id).set(newTask)
                            .addOnSuccessListener {
                                tasks.add(newTask)
                                showDialog = false
                            }.addOnFailureListener { exception ->

                            Log.e("TaskScreen", "Error al agregar tarea: ${exception.message}")
                        }
                    }
                )
            }

            if (showDialog && taskToEdit != null) {
                EditTaskDialog(
                    task = taskToEdit!!,
                    onDismiss = { showDialog = false },
                    onSave = { updatedTask ->
                        tasksCollection.document(taskToEdit!!.id).set(updatedTask)
                            .addOnSuccessListener {
                                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                                if (index != -1) {
                                    tasks[index] = updatedTask
                                }
                                showDialog = false
                            }
                    }
                )
            }
        }
    )
}

@Composable
fun TaskItem(
    task: Task,
    onTaskUpdated: (Task) -> Unit,
    onStateChanged: (Task) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onTaskUpdated(task)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = task.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = task.description, style = MaterialTheme.typography.bodySmall)
        }
        Checkbox(
            checked = task.completed,
            onCheckedChange = { isChecked ->
                val updatedTask = task.copy(completed = isChecked)
                onStateChanged(updatedTask)
            }
        )
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAddTask: (Task) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Tarea") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTask = Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = description,
                        completed = false
                    )
                    onAddTask(newTask)
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tarea") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedTask = task.copy(title = title, description = description)
                    onSave(updatedTask)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    PocketPalTheme {

    }
}


