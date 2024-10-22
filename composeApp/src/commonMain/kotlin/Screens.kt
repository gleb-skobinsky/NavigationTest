import kotlinx.serialization.Serializable

@Serializable
sealed interface Screens {
    @Serializable
    data object FirstScreen : Screens

    @Serializable
    data class SecondScreen(val parameter: String) : Screens

    @Serializable
    data object NestedFlow : Screens {
        @Serializable
        data object FirstNestedScreen : Screens

        @Serializable
        data object SecondNestedScreen : Screens
    }
}