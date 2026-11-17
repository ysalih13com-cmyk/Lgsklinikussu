package com.example.gemini

import com.example.data.WhiteboardElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TeacherBeat(
    val speak: String,
    val drawCommands: List<TeacherDrawCommand> = emptyList()
)

data class TeacherDrawCommand(
    val type: String, // write, line, arrow, circle, rect, underline, erase, clear
    val text: String? = null,
    val x1: Float = 0f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 0f,
    val radius: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f,
    val color: String? = null,
    val fontSize: Float = 22f
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askTeacher(
        apiKey: String,
        prompt: String,
        character: String,
        boardElements: List<WhiteboardElement>,
        chatHistory: List<Pair<Boolean, String>> // isUser -> text
    ): List<TeacherBeat> = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return@withContext generateOfflineTeacherResponse(prompt, character)
        }

        try {
            val systemPrompt = buildSystemPrompt(character, boardElements)
            val contentsArray = JSONArray()

            // Add recent history (up to last 10 messages)
            chatHistory.takeLast(10).forEach { (isUser, text) ->
                val role = if (isUser) "user" else "model"
                val partObj = JSONObject().put("text", text)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }

            // Current prompt
            val currentPart = JSONObject().put("text", prompt)
            val currentContent = JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(currentPart))
            contentsArray.put(currentContent)

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            // Use supported model as per gemini-api skill: gemini-3.5-flash
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$trimmedKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string().orEmpty()
                return@withContext listOf(
                    TeacherBeat("Gemini bağlantısında bir sorun oluştu (${response.code}). İnternetini ve API anahtarını kontrol edebilirsin. Yine de sorunu cevaplayayım: " + getCurriculumHint(prompt))
                )
            }

            val bodyStr = response.body?.string() ?: ""
            parseTeacherResponse(bodyStr)
        } catch (e: Exception) {
            e.printStackTrace()
            generateOfflineTeacherResponse(prompt, character)
        }
    }

    private fun buildSystemPrompt(character: String, boardElements: List<WhiteboardElement>): String {
        val charDesc = when (character) {
            "enerjik" -> "Enerjik, esprili ve heyecanlı bir LGS hocasısın. Coşkulu bir anlatım yap."
            "mat" -> "Matematik odaklı, formül ve adımları net veren, mantık kurduran titiz bir hocasın."
            "fen" -> "Fen bilimleri odaklı, günlük hayattan somut örnekler ve deneyler veren bir hocasın."
            "tur" -> "Türkçe odaklı, kelimelerin ve paragrafların inceliğini hikâyeleştiren bir hocasın."
            else -> "Sakin, sabırlı ve anlaşılır bir üslupla konuşan Prof. Dr. Yusuf Salih'sin."
        }

        val boardSummary = boardElements.takeLast(15).map {
            mapOf("type" to it.type, "text" to (it.text ?: ""), "x" to it.x1.toInt(), "y" to it.y1.toInt())
        }

        return """
            Sen LGS 2027 Klinik Üssü'nün kurucusu Prof. Dr. Yusuf Salih'sin. 8. sınıf LGS öğrencilerine rehberlik eden samimi bir öğretmensin.
            Üslup: $charDesc
            Kurallar:
            - Doğal, cesaretlendirici bir Türkçe kullan.
            - Öğrenci tahtadaki bir şeyi sorduğunda mevcut tahta nesnelerini dikkate al.
            - Tahta koordinatları: x: 0-800, y: 0-450.
            - Cevabını YALNIZCA aşağıdaki JSON formatında ver. Başka hiçbir metin veya markdown ekleme:
            {
              "beats": [
                {
                  "speak": "Öğretmenin söyleyeceği 1-2 cümlelik konuşma.",
                  "draw": [
                    { "type": "write", "text": "Formül veya kilit kavram", "x1": 50, "y1": 50, "color": "#0284c7", "fontSize": 24 }
                  ]
                }
              ]
            }
            draw türleri: write, line, arrow, circle, rect, underline, clear
            Tahtadaki mevcut nesneler: $boardSummary
        """.trimIndent()
    }

    private fun parseTeacherResponse(rawJson: String): List<TeacherBeat> {
        val beatsList = mutableListOf<TeacherBeat>()
        try {
            val root = JSONObject(rawJson)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val firstCandidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = firstCandidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val textPart = parts.optJSONObject(0)?.optString("text").orEmpty().trim()

            val cleanText = textPart.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanText)
            val beatsArray = parsed.optJSONArray("beats") ?: return emptyList()

            for (i in 0 until beatsArray.length()) {
                val beatObj = beatsArray.optJSONObject(i) ?: continue
                val speak = beatObj.optString("speak", "")
                val drawsList = mutableListOf<TeacherDrawCommand>()
                val drawsArray = beatObj.optJSONArray("draw")
                if (drawsArray != null) {
                    for (j in 0 until drawsArray.length()) {
                        val dObj = drawsArray.optJSONObject(j) ?: continue
                        drawsList.add(
                            TeacherDrawCommand(
                                type = dObj.optString("type", "write"),
                                text = if (dObj.has("text")) dObj.optString("text") else null,
                                x1 = dObj.optDouble("x1", dObj.optDouble("x", 50.0)).toFloat(),
                                y1 = dObj.optDouble("y1", dObj.optDouble("y", 50.0)).toFloat(),
                                x2 = dObj.optDouble("x2", 150.0).toFloat(),
                                y2 = dObj.optDouble("y2", 150.0).toFloat(),
                                radius = dObj.optDouble("radius", dObj.optDouble("r", 30.0)).toFloat(),
                                width = dObj.optDouble("width", dObj.optDouble("w", 100.0)).toFloat(),
                                height = dObj.optDouble("height", dObj.optDouble("h", 60.0)).toFloat(),
                                color = dObj.optString("color", "#0284c7"),
                                fontSize = dObj.optDouble("fontSize", 24.0).toFloat()
                            )
                        )
                    }
                }
                beatsList.add(TeacherBeat(speak, drawsList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return if (beatsList.isNotEmpty()) beatsList else listOf(TeacherBeat("Harika bir soru! Şimdi tahtaya odaklan ve birlikte inceleyelim."))
    }

    private fun generateOfflineTeacherResponse(prompt: String, character: String): List<TeacherBeat> {
        val lower = prompt.lowercase()
        return when {
            "çarpan" in lower || "ebob" in lower || "ekok" in lower -> {
                listOf(
                    TeacherBeat(
                        speak = "Çarpanlar ve Katlar LGS'nin temelidir! Bir sayıyı kalansız bölen pozitif tam sayılara o sayının çarpanları diyoruz.",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "Pozitif Çarpanlar & EBOB-EKOK", x1 = 40f, y1 = 40f, color = "#0284c7", fontSize = 24f),
                            TeacherDrawCommand(type = "write", text = "12 = 1·12 = 2·6 = 3·4", x1 = 40f, y1 = 90f, color = "#0ea5e9", fontSize = 22f)
                        )
                    ),
                    TeacherBeat(
                        speak = "EBOB için ortak asalların en küçük üslerini, EKOK için ise tüm asal çarpanların en büyük üslerini çarparız!",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "rect", x1 = 30f, y1 = 140f, width = 450f, height = 90f, color = "#16a34a"),
                            TeacherDrawCommand(type = "write", text = "EBOB(12,18) = 6  |  EKOK(12,18) = 36", x1 = 45f, y1 = 165f, color = "#16a34a", fontSize = 22f)
                        )
                    )
                )
            }
            "üslü" in lower -> {
                listOf(
                    TeacherBeat(
                        speak = "Üslü sayılarda tabanlar aynı olduğunda çarparken üsler toplanır, bölerken üsler çıkarılır!",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "aᵐ · aⁿ = aᵐ⁺ⁿ", x1 = 50f, y1 = 60f, color = "#0284c7", fontSize = 26f),
                            TeacherDrawCommand(type = "write", text = "aᵐ ÷ aⁿ = aᵐ⁻ⁿ", x1 = 50f, y1 = 110f, color = "#0ea5e9", fontSize = 26f)
                        )
                    ),
                    TeacherBeat(
                        speak = "Ayrıca sıfır hariç herhangi bir sayının sıfırıncı kuvveti her zaman 1'dir. Negatif üs ise tabanı ters çevirir!",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "a⁰ = 1  ve  a⁻ⁿ = 1/aⁿ", x1 = 50f, y1 = 170f, color = "#dc2626", fontSize = 22f)
                        )
                    )
                )
            }
            "karekök" in lower -> {
                listOf(
                    TeacherBeat(
                        speak = "Kareköklü sayılarda çarpma yaparken kök içleri çarpılır: √a · √b = √(a·b).",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "√a · √b = √(a·b)", x1 = 50f, y1 = 60f, color = "#0284c7", fontSize = 26f),
                            TeacherDrawCommand(type = "write", text = "√50 = √(25·2) = 5√2", x1 = 50f, y1 = 110f, color = "#16a34a", fontSize = 24f)
                        )
                    ),
                    TeacherBeat(
                        speak = "Dikkat: Toplama ve çıkarmada kökler birleştirilmez! Yalnızca kök içi aynı olan terimlerin katsayıları toplanır.",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "❌ √4 + √9 ≠ √13  (2 + 3 = 5)", x1 = 50f, y1 = 170f, color = "#dc2626", fontSize = 22f)
                        )
                    )
                )
            }
            "basınç" in lower -> {
                listOf(
                    TeacherBeat(
                        speak = "Basınç birim yüzeye dik etki eden kuvvettir. Katı basıncı formülü: P = F / A.",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "Katı Basıncı: P = F / A", x1 = 50f, y1 = 60f, color = "#0284c7", fontSize = 26f),
                            TeacherDrawCommand(type = "write", text = "Yüzey Alanı (A) ↓ ise Basınç (P) ↑", x1 = 50f, y1 = 110f, color = "#16a34a", fontSize = 22f)
                        )
                    ),
                    TeacherBeat(
                        speak = "Bıçakların bilenmesi yüzey alanını küçülterek basıncı artırır; kar ayakkabıları ise alanı büyüterek batmayı önler!",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "rect", x1 = 40f, y1 = 150f, width = 450f, height = 80f, color = "#ea580c"),
                            TeacherDrawCommand(type = "write", text = "Sıvı Basıncı: P = h · d · g", x1 = 50f, y1 = 175f, color = "#ea580c", fontSize = 24f)
                        )
                    )
                )
            }
            "fiilimsi" in lower -> {
                listOf(
                    TeacherBeat(
                        speak = "Fiilimsiler fiil kökünden türer ama cümlede isim, sıfat veya zarf görevinde kullanılır. Üç çeşidi vardır:",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "İsim-fiil: -ma, -ış, -mak (mayışmak)", x1 = 40f, y1 = 60f, color = "#0284c7", fontSize = 22f),
                            TeacherDrawCommand(type = "write", text = "Sıfat-fiil: -an, -ası, -mez, -ar, -dik, -ecek, -miş", x1 = 40f, y1 = 100f, color = "#16a34a", fontSize = 22f),
                            TeacherDrawCommand(type = "write", text = "Zarf-fiil: -ip, -erek, -ince, -ken, -dıkça", x1 = 40f, y1 = 140f, color = "#ea580c", fontSize = 22f)
                        )
                    ),
                    TeacherBeat(
                        speak = "Unutma, fiilimsiler tek başlarına çekimli fiil gibi yüklem oluşturmazlar. Şimdi örnek cümle yazalım.",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "Örnek: \"Koşarak gelen çocuk güldü.\"", x1 = 40f, y1 = 190f, color = "#0f172a", fontSize = 22f)
                        )
                    )
                )
            }
            else -> {
                listOf(
                    TeacherBeat(
                        speak = "Harika bir konuya değindin! Bu konuyu LGS sınavında soru kaçırmayacak şekilde adım adım tahtada çözelim.",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "write", text = "Prof. Dr. Yusuf Salih · Ders Notu", x1 = 50f, y1 = 50f, color = "#0284c7", fontSize = 24f),
                            TeacherDrawCommand(type = "line", x1 = 50f, y1 = 85f, x2 = 450f, y2 = 85f, color = "#0284c7"),
                            TeacherDrawCommand(type = "write", text = "1. Soru kökünü dikkatle oku", x1 = 50f, y1 = 110f, color = "#0f172a", fontSize = 22f),
                            TeacherDrawCommand(type = "write", text = "2. Verilenleri ve isteneni belirle", x1 = 50f, y1 = 150f, color = "#0f172a", fontSize = 22f),
                            TeacherDrawCommand(type = "write", text = "3. Adım adım sonuca ulaş", x1 = 50f, y1 = 190f, color = "#16a34a", fontSize = 22f)
                        )
                    ),
                    TeacherBeat(
                        speak = "Takıldığın her an mikrofonu açıp sorabilirsin, birlikte başaracağız!",
                        drawCommands = listOf(
                            TeacherDrawCommand(type = "circle", x1 = 500f, y1 = 150f, radius = 40f, color = "#16a34a"),
                            TeacherDrawCommand(type = "write", text = "🎯 Hedef 500", x1 = 460f, y1 = 135f, color = "#16a34a", fontSize = 18f)
                        )
                    )
                )
            }
        }
    }

    private fun getCurriculumHint(prompt: String): String {
        return "Bu konuyu çalışırken temel formülleri defterine not et ve soru bankasından en az 10 soru çözerek pekiştir!"
    }
}
