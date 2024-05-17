import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.reflect.KClass

object UtilColors {
    val Wheat = Color(245, 210, 200)
    val LightBlue = Color(200, 210, 245)
}

class FirstScreenViewModel : ViewModel() {
    var firstScreenInput by mutableStateOf("")
        private set

    fun updateInput(newValue: String) {
        firstScreenInput = newValue
    }

    companion object {
        val Factory: ViewModelProvider.Factory? = when (getPlatform()) {
            Platform.IOS -> defaultFactory { FirstScreenViewModel() }
            Platform.Android -> null
        }
    }
}

class SecondScreenViewModel : ViewModel() {
    var secondScreenInput by mutableStateOf("")
        private set

    fun updateInput(newValue: String) {
        secondScreenInput = newValue
    }

    companion object {
        val Factory: ViewModelProvider.Factory? = when (getPlatform()) {
            Platform.IOS -> defaultFactory { SecondScreenViewModel() }
            Platform.Android -> null
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> defaultFactory(producer: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(
            modelClass: KClass<T>,
            extras: CreationExtras
        ): T {
            return producer() as T
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        FilledCenteredBox(Color.LightGray) {
            val navController = rememberNavController()
            NavHost(navController, "start_screen") {
                composable("start_screen") { entry ->
                    val viewModel =
                        viewModel(
                            FirstScreenViewModel::class,
                            viewModelStoreOwner = entry,
                            factory = FirstScreenViewModel.Factory
                        )
                    FilledCenteredBox(Color.Cyan) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(entry.destination.route.orEmpty())
                            TextField(viewModel.firstScreenInput, { viewModel.updateInput(it) })
                            Button({ navController.navigate("second_screen") }) {
                                Text("Go forward")
                            }
                        }
                    }
                }
                composable("second_screen") { entry ->
                    val viewModel =
                        viewModel(
                            SecondScreenViewModel::class,
                            viewModelStoreOwner = entry,
                            factory = SecondScreenViewModel.Factory
                        )
                    FilledCenteredBox(Color.Yellow) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(entry.destination.route.orEmpty())
                            TextField(viewModel.secondScreenInput, { viewModel.updateInput(it) })
                            Button({ navController.popBackStack() }) {
                                Text("Go back")
                            }
                            Button({ navController.navigate("nested_screen1") }) {
                                Text("Go to nested")
                            }
                        }
                    }
                }
                navigation("nested_screen1", "nested") {
                    composable("nested_screen1") { entry ->
                        val sharedViewModel: FirstScreenViewModel =
                            entry.sharedViewModel(navController, FirstScreenViewModel.Factory)
                        FilledCenteredBox(UtilColors.Wheat) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(entry.destination.route.orEmpty())
                                TextField(
                                    sharedViewModel.firstScreenInput,
                                    { sharedViewModel.updateInput(it) })
                                Button({ navController.popBackStack() }) {
                                    Text("Go back")
                                }
                                Button({ navController.navigate("nested_screen2") }) {
                                    Text("Go to nested 2")
                                }
                            }
                        }
                    }
                    composable("nested_screen2") { entry ->
                        val sharedViewModel: FirstScreenViewModel =
                            entry.sharedViewModel(navController, FirstScreenViewModel.Factory)
                        FilledCenteredBox(UtilColors.LightBlue) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(entry.destination.route.orEmpty())
                                Text(sharedViewModel.firstScreenInput)
                                Button({ navController.popBackStack() }) {
                                    Text("Go back")
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
inline fun <reified VM : ViewModel> NavBackStackEntry.sharedViewModel(
    navController: NavController,
    factory: ViewModelProvider.Factory?
): VM {
    val navRoute = destination.parent?.route ?: return viewModel(
        modelClass = VM::class,
        factory = factory
    )
    val parentEntry = remember(this) {
        try {
            navController.getBackStackEntry(navRoute)
        } catch (e: Exception) {
            this
        }
    }
    return viewModel(modelClass = VM::class, viewModelStoreOwner = parentEntry, factory = factory)
}

@Composable
fun FilledCenteredBox(color: Color, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(color),
        contentAlignment = Alignment.Center,
        content = content
    )
}