import io.litequest.demo.PaginatedViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

fun main() {
    val viewModel = PaginatedViewModel()
    
    // Update has-diabetes
    viewModel.updateAnswer("has-diabetes", JsonPrimitive(true))
    
    // Get the response and print structure
    val response = viewModel.state.value.response
    val json = Json { prettyPrint = true }
    val jsonString = json.encodeToString(
        io.litequest.model.QuestionnaireResponse.serializer(),
        response
    )
    
    println("Response structure:")
    println(jsonString)
    
    // Also print the questionnaire structure
    println("\nQuestionnaire items:")
    response.items.forEach { item ->
        println("- ${item.linkId} (has ${item.items.size} nested items)")
        item.items.forEach { nested ->
            println("  - ${nested.linkId}")
        }
    }
}
