package com.example.data

object LgsCurriculum {

    val courses: List<Course> = listOf(
        Course(
            id = "mat",
            name = "Matematik",
            icon = "📐",
            units = listOf(
                "Çarpanlar ve Katlar", "Üslü İfadeler", "Kareköklü İfadeler", "Veri Analizi",
                "Olasılık", "Cebirsel İfadeler", "Doğrusal Denklemler", "Eşitsizlikler",
                "Üçgenler", "Dönüşüm Geometrisi", "Geometrik Cisimler"
            )
        ),
        Course(
            id = "fen",
            name = "Fen Bilimleri",
            icon = "🧪",
            units = listOf(
                "Mevsimler ve İklim", "DNA ve Genetik Kod", "Basınç", "Madde ve Endüstri",
                "Basit Makineler", "Enerji Dönüşümleri", "Elektrik Yükleri"
            )
        ),
        Course(
            id = "tur",
            name = "Türkçe",
            icon = "📖",
            units = listOf(
                "Fiilimsiler", "Cümlenin Ögeleri", "Cümle Türleri", "Fiillerde Çatı",
                "Paragraf", "Yazım ve Noktalama", "Söz Sanatları"
            )
        ),
        Course(
            id = "ink",
            name = "T.C. İnkılap Tarihi",
            icon = "🏛️",
            units = listOf(
                "Bir Kahraman Doğuyor", "Milli Uyanış", "Milli Bir Destan",
                "Atatürkçülük", "Demokratikleşme Çabaları", "Dış Politika"
            )
        ),
        Course(
            id = "ing",
            name = "İngilizce",
            icon = "🌍",
            units = listOf(
                "Friendship", "Teen Life", "In the Kitchen", "On the Phone",
                "The Internet", "Adventures", "Tourism", "Chores", "Science", "Natural Forces"
            )
        ),
        Course(
            id = "din",
            name = "Din Kültürü",
            icon = "🌙",
            units = listOf(
                "Kader İnancı", "Zekât ve Sadaka", "Din ve Hayat",
                "Hz. Muhammed'in Örnekliği", "Kur'an-ı Kerim ve Yorumu"
            )
        )
    )

    val coursesMap: Map<String, Course> = courses.associateBy { it.id }

    val flashcards: List<FlashCard> = listOf(
        FlashCard("mat", "Üslü İfadeler", "a⁰ = ?", "1 (a ≠ 0). Sıfırıncı kuvvet her zaman 1."),
        FlashCard("mat", "Üslü İfadeler", "a⁻ⁿ = ?", "1/aⁿ : eksi işaret tabanı ters çevirir."),
        FlashCard("mat", "Üslü İfadeler", "Bilimsel gösterim", "a × 10ⁿ ve 1 ≤ a < 10"),
        FlashCard("mat", "Kareköklü İfadeler", "√(a·b) = ?", "√a · √b : çarpımın kökü, köklerin çarpımı."),
        FlashCard("mat", "Çarpanlar ve Katlar", "EBOB / EKOK", "EBOB: ortak asal çarpanların en küçük üsleri. EKOK: tüm asal çarpanların en büyük üsleri."),
        FlashCard("mat", "Üçgenler", "Üçgen eşitsizliği", "|b−c| < a < b+c"),
        FlashCard("mat", "Cebirsel İfadeler", "(a+b)² = ?", "a² + 2ab + b²"),
        FlashCard("mat", "Cebirsel İfadeler", "a² − b² = ?", "(a−b)(a+b)"),
        FlashCard("mat", "Olasılık", "Olasılık formülü", "İstenen durum sayısı / Tüm durum sayısı (0 ile 1 arası)."),
        FlashCard("fen", "Mevsimler ve İklim", "Mevsimler neden oluşur?", "Dünya'nın eksen eğikliği + Güneş etrafındaki dolanımı."),
        FlashCard("fen", "DNA ve Genetik Kod", "Gen → DNA → Kromozom", "Gen en küçük, kromozom en büyük birimdir. Nükleotit = fosfat + şeker + baz."),
        FlashCard("fen", "DNA ve Genetik Kod", "Baz eşleşmesi", "A–T ve G–C."),
        FlashCard("fen", "Basınç", "Katı basıncı", "P = F / A : yüzey küçüldükçe basınç artar."),
        FlashCard("fen", "Basınç", "Sıvı basıncı", "P = h·d·g : derinlik ve yoğunlukla artar, kabın şekline bağlı değildir."),
        FlashCard("fen", "Madde ve Endüstri", "pH skalası", "pH < 7 asit, pH = 7 nötr, pH > 7 baz."),
        FlashCard("fen", "Elektrik Yükleri", "Aynı / zıt yükler", "Aynı yükler iter, zıt yükler çeker."),
        FlashCard("tur", "Fiilimsiler", "Fiilimsi ekleri", "İsim-fiil: -ma, -ış, -mek · Sıfat-fiil: -an, -esi, -mez · Zarf-fiil: -ip, -arak, -ince"),
        FlashCard("tur", "Cümlenin Ögeleri", "Yüklem nasıl bulunur?", "Cümlenin sonundaki eylem (genelde). \"Ne yapıyor?\" sorusuyla ara."),
        FlashCard("tur", "Fiillerde Çatı", "Edilgen çatı", "Özne belli değildir; -ıl / -ın eki alır (yazıldı)."),
        FlashCard("tur", "Söz Sanatları", "Kişileştirme", "İnsana özgü özelliklerin insan dışı varlığa verilmesi."),
        FlashCard("tur", "Paragraf", "Ana düşünce", "Yazarın okura vermek istediği asıl mesaj; genelde son cümle ya da sonuç bölümü."),
        FlashCard("ink", "Milli Uyanış", "Amasya Genelgesi", "\"Vatanın bütünlüğü, milletin istiklali tehlikededir.\" Milli mücadelenin gerekçe ve amacı."),
        FlashCard("ink", "Milli Bir Destan", "Sakarya Meydan Muharebesi", "\"Hattı müdafaa yoktur, sathı müdafaa vardır.\" Sonrası Mareşal rütbesi."),
        FlashCard("ink", "Atatürkçülük", "Altı ok", "Cumhuriyetçilik, milliyetçilik, halkçılık, devletçilik, laiklik, inkılapçılık."),
        FlashCard("ink", "Bir Kahraman Doğuyor", "Atatürk'ün doğumu", "1881, Selanik."),
        FlashCard("ing", "Teen Life", "Simple Past nasıl kurulur?", "V2 / -ed. Olumsuz: didn't + V1. Soru: Did + özne + V1."),
        FlashCard("ing", "The Internet", "Comparative", "Kısa sıfat + er than; uzun sıfat: more + sıfat + than."),
        FlashCard("ing", "Friendship", "Would you like to...?", "Davet: Yes, I'd love to. / Sorry, I can't."),
        FlashCard("din", "Kader İnancı", "Kader nedir?", "Allah'ın bilgisi ve takdiridir; insanın iradesi ve sorumluluğu vardır."),
        FlashCard("din", "Zekât ve Sadaka", "Zekât kimlere verilir?", "Fakir, miskin gibi 8 sınıfa; anne-baba ve çocuklara verilmez."),
        FlashCard("din", "Din ve Hayat", "Ahlaki değer", "İyilik, dürüstlük, yardımlaşma insanı olgunlaştıran değerlerdir."),
        FlashCard("mat", "Veri Analizi", "Ortalama (aritmetik ortalama)", "Verilerin toplamı ÷ veri sayısı."),
        FlashCard("mat", "Veri Analizi", "Ortanca (medyan) nedir?", "Küçükten büyüğe sıralandığında ortadaki değer; çiftse ortadaki ikisinin ortalaması."),
        FlashCard("mat", "Veri Analizi", "Tepe değer (mod) nedir?", "Bir veri grubunda en çok tekrar eden değer."),
        FlashCard("mat", "Doğrusal Denklemler", "Doğrunun eğimi (m)", "m = (y₂−y₁)/(x₂−x₁)"),
        FlashCard("mat", "Doğrusal Denklemler", "y = mx + n", "n: doğrunun y eksenini kestiği nokta."),
        FlashCard("mat", "Doğrusal Denklemler", "Paralel doğrular", "Eğimleri birbirine eşittir."),
        FlashCard("mat", "Eşitsizlikler", "Negatifle çarpma/bölme", "Eşitsizlik yön değiştirir (< ↔ >)."),
        FlashCard("mat", "Eşitsizlikler", "x > 3 sayı doğrusu", "3 noktası içi boş daire, sağ taraf taranır."),
        FlashCard("mat", "Dönüşüm Geometrisi", "Ötelemede boyut", "Değişmez, sadece konum değişir."),
        FlashCard("mat", "Dönüşüm Geometrisi", "Yansıma (simetri)", "Simetri eksenine göre ayna görüntüsü alınır."),
        FlashCard("mat", "Geometrik Cisimler", "Küpün hacmi", "V = a³ (a: bir kenar uzunluğu)."),
        FlashCard("mat", "Geometrik Cisimler", "Dik prizma hacmi", "V = Taban alanı × yükseklik."),
        FlashCard("mat", "Kareköklü İfadeler", "√a / √b = ?", "√(a/b) : bölümün kökü, köklerin bölümüne eşittir (b ≠ 0)."),
        FlashCard("mat", "Çarpanlar ve Katlar", "Asal sayı nedir?", "1 ve kendisinden başka pozitif böleni olmayan, 1'den büyük doğal sayı."),
        FlashCard("mat", "Çarpanlar ve Katlar", "Pozitif çarpan sayısı", "Asal çarpanlarına ayır, her asalın üssüne 1 ekleyip çarp."),
        FlashCard("mat", "Üçgenler", "İç açılar toplamı", "180°"),
        FlashCard("mat", "Olasılık", "İmkânsız olay", "0"),
        FlashCard("fen", "Basit Makineler", "Kaldıraçta kuvvet kazancı", "Kuvvet kolu, yük kolundan uzun olduğunda kuvvet kazancı artar."),
        FlashCard("fen", "Basit Makineler", "Makara sistemleri", "Yükü kaldırmak için gereken kuvveti azaltır ya da yön değiştirir."),
        FlashCard("fen", "Enerji Dönüşümleri", "Enerjinin korunumu", "Enerji yoktan var, vardan yok edilemez; bir türden diğerine dönüşür."),
        FlashCard("fen", "Enerji Dönüşümleri", "Pilde enerji dönüşümü", "Kimyasal enerji → elektrik enerjisi."),
        FlashCard("fen", "Mevsimler ve İklim", "Gölge boyu ne zaman en kısa?", "Güneş tam tepedeyken (öğle vakti civarı)."),
        FlashCard("fen", "Madde ve Endüstri", "Nötrleşme tepkimesi", "Asit + Baz → Tuz + Su."),
        FlashCard("fen", "Elektrik Yükleri", "Topraklama", "Statik yükü güvenli şekilde toprağa aktarıp boşaltmak için."),
        FlashCard("tur", "Cümle Türleri", "Anlamına göre cümleler", "Olumlu, olumsuz, soru, ünlem cümlesi."),
        FlashCard("tur", "Cümle Türleri", "Yüklemin türüne göre", "Fiil cümlesi ve isim (ad) cümlesi."),
        FlashCard("tur", "Yazım ve Noktalama", "\"de / da\" yazımı", "Bağlaç olduğunda (yerine \"dahi\" getirilebiliyorsa) ayrı yazılır."),
        FlashCard("tur", "Yazım ve Noktalama", "Kısaltmalara gelen ekler", "Kesme işareti (') ile ayrılır: TDK'nin gibi."),
        FlashCard("tur", "Fiilimsiler", "Fiilimsilerin özelliği", "Hem fiil hem isim özelliği taşırlar ama tek başlarına yüklem olamazlar."),
        FlashCard("tur", "Cümlenin Ögeleri", "Nesne nasıl bulunur?", "Yükleme \"neyi / kimi\" sorusu sorularak bulunur."),
        FlashCard("tur", "Fiillerde Çatı", "Dönüşlü çatı", "Öznenin yaptığı işin yine kendi üzerinde kalmasıdır (-ın/-in eki): Yıkandı."),
        FlashCard("tur", "Söz Sanatları", "Teşbih (benzetme)", "Bir varlığın nitelik yönünden başka bir varlığa benzetilmesi."),
        FlashCard("ink", "Demokratikleşme Çabaları", "İlk muhalefet partisi", "Terakkiperver Cumhuriyet Fırkası (1924)."),
        FlashCard("ink", "Demokratikleşme Çabaları", "Kadınlara milletvekili seçme-seçilme", "1934."),
        FlashCard("ink", "Dış Politika", "Yurtta Sulh, Cihanda Sulh", "Atatürk'ün barışçı ve gerçekçi dış politika ilkesi."),
        FlashCard("din", "Hz. Muhammed'in Örnekliği", "Muhammedü'l-Emin", "Doğruluğu, dürüstlüğü ve güvenilirliğiyle tanınması sebebiyle peygamberlik öncesi verilmiştir."),
        FlashCard("din", "Kur'an-ı Kerim ve Yorumu", "Tefsir ve Meal farkı", "Meal: kısa tercüme. Tefsir: ayetlerin ayrıntılı açıklanması ve yorumlanmasıdır.")
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            courseId = "mat",
            unit = "Üslü İfadeler",
            question = "2³ · 2⁴ işleminin sonucu kaçtır?",
            options = listOf("2⁷", "2¹²", "4⁷", "2¹"),
            correctIndex = 0,
            explanation = "Taban aynı olduğunda üsler toplanır: 3 + 4 = 7."
        ),
        QuizQuestion(
            courseId = "mat",
            unit = "Üslü İfadeler",
            question = "(−3)² + 3⁰ kaçtır?",
            options = listOf("8", "10", "6", "−8"),
            correctIndex = 1,
            explanation = "(−3)² = 9, 3⁰ = 1. Toplam: 9 + 1 = 10."
        ),
        QuizQuestion(
            courseId = "mat",
            unit = "Kareköklü İfadeler",
            question = "√50 ifadesi aşağıdakilerden hangisine eşittir?",
            options = listOf("5√2", "2√5", "25√2", "10√5"),
            correctIndex = 0,
            explanation = "50 = 25 · 2 → √25 · √2 = 5√2."
        ),
        QuizQuestion(
            courseId = "mat",
            unit = "Çarpanlar ve Katlar",
            question = "12 ve 18 sayılarının EBOB'u kaçtır?",
            options = listOf("3", "6", "9", "36"),
            correctIndex = 1,
            explanation = "12 = 2² · 3, 18 = 2 · 3² → Ortak asal çarpanların en küçük üsleri: 2¹ · 3¹ = 6."
        ),
        QuizQuestion(
            courseId = "mat",
            unit = "Cebirsel İfadeler",
            question = "(x + 3)(x − 3) ifadesi neye eşittir?",
            options = listOf("x² − 9", "x² + 9", "x² − 6", "x² + 6x − 9"),
            correctIndex = 0,
            explanation = "İki kare farkı özdeşliği: (a+b)(a−b) = a² − b² → x² − 9."
        ),
        QuizQuestion(
            courseId = "mat",
            unit = "Olasılık",
            question = "İçinde 3 kırmızı, 2 mavi top olan torbadan çekilen topun mavi olma olasılığı?",
            options = listOf("2/3", "2/5", "3/5", "1/2"),
            correctIndex = 1,
            explanation = "İstenen mavi top sayısı = 2, toplam top sayısı = 3 + 2 = 5. Olasılık: 2/5."
        ),
        QuizQuestion(
            courseId = "fen",
            unit = "Mevsimler ve İklim",
            question = "Mevsimlerin oluşmasının temel nedeni nedir?",
            options = listOf("Dünya'nın Güneş'e uzaklığı", "Eksen eğikliği ve dolanma", "Ay'ın çekimi", "Gece-gündüz oluşu"),
            correctIndex = 1,
            explanation = "Dünya'nın 23°27'lik eksen eğikliği ve Güneş etrafında dolanması mevsimleri oluşturur."
        ),
        QuizQuestion(
            courseId = "fen",
            unit = "DNA ve Genetik Kod",
            question = "DNA'da adenin (A) hangi baz ile eşleşir?",
            options = listOf("Guanin", "Sitozin", "Timin", "Urasil"),
            correctIndex = 2,
            explanation = "DNA zincirinde Adenin (A) Timin (T) ile, Guanin (G) Sitozin (C) ile eşleşir."
        ),
        QuizQuestion(
            courseId = "fen",
            unit = "Basınç",
            question = "Bir katının basıncını artırmak için ne yapılmalıdır?",
            options = listOf("Yüzey alanı artırılmalı", "Yüzey alanı azaltılmalı", "Kuvvet azaltılmalı", "Hacim artırılmalı"),
            correctIndex = 1,
            explanation = "P = F / A formülüne göre yüzey alanı (A) küçüldükçe katı basıncı (P) artar."
        ),
        QuizQuestion(
            courseId = "fen",
            unit = "Madde ve Endüstri",
            question = "pH değeri 3 olan çözelti nedir?",
            options = listOf("Baz", "Nötr", "Asit", "Tuz"),
            correctIndex = 2,
            explanation = "pH ölçeğinde 0-7 arası asidik, 7 nötr, 7-14 arası baziktir."
        ),
        QuizQuestion(
            courseId = "tur",
            unit = "Fiilimsiler",
            question = "\"Koşarak okula geldi.\" cümlesindeki fiilimsi türü nedir?",
            options = listOf("İsim-fiil", "Sıfat-fiil", "Zarf-fiil", "Ortaç"),
            correctIndex = 2,
            explanation = "\"-arak / -erek\" eki zarf-fiil (ulaç) ekidir."
        ),
        QuizQuestion(
            courseId = "tur",
            unit = "Fiillerde Çatı",
            question = "\"Ödevler zamanında yapıldı.\" cümlesinin çatısı nedir?",
            options = listOf("Etken", "Edilgen", "Dönüşlü", "İşteş"),
            correctIndex = 1,
            explanation = "İşi yapan özne belli değildir ve fiil \"-ıl\" eki almıştır: Edilgen çatı."
        ),
        QuizQuestion(
            courseId = "tur",
            unit = "Söz Sanatları",
            question = "\"Rüzgâr fısıldadı.\" cümlesinde hangi söz sanatı var?",
            options = listOf("Abartma", "Kişileştirme", "Benzetme", "Zıtlık"),
            correctIndex = 1,
            explanation = "İnsana özgü \"fısıldamak\" eylemi rüzgâra aktarıldığı için kişileştirme (teşhis) yapılmıştır."
        ),
        QuizQuestion(
            courseId = "ink",
            unit = "Milli Bir Destan",
            question = "Sakarya Meydan Muharebesi sonrası Mustafa Kemal'e ne verildi?",
            options = listOf("Mareşallik ve Gazilik", "Sadrazamlık", "Hilafet", "Nişan"),
            correctIndex = 0,
            explanation = "Sakarya Zaferi'nden sonra TBMM tarafından Mareşal rütbesi ve Gazi unvanı tevcih edildi."
        ),
        QuizQuestion(
            courseId = "ink",
            unit = "Atatürkçülük",
            question = "Aşağıdakilerden hangisi Atatürk ilkelerinden biri değildir?",
            options = listOf("Halkçılık", "Laiklik", "Devletçilik", "Krallık"),
            correctIndex = 3,
            explanation = "Atatürk'ün altı temel ilkesi: Cumhuriyetçilik, Milliyetçilik, Halkçılık, Devletçilik, Laiklik, İnkılapçılıktır."
        ),
        QuizQuestion(
            courseId = "ink",
            unit = "Milli Uyanış",
            question = "Milli mücadelenin gerekçesini ve amacını ilk kez ilan eden belge?",
            options = listOf("Erzurum Kongresi", "Amasya Genelgesi", "Misak-ı Milli", "Sivas Kongresi"),
            correctIndex = 1,
            explanation = "1919 tarihli Amasya Genelgesi, Kurtuluş Savaşı'nın gerekçe, amaç ve yöntemini ilk kez belirten belgedir."
        ),
        QuizQuestion(
            courseId = "ing",
            unit = "Teen Life",
            question = "Choose the correct form: \"Yesterday I ___ to the cinema.\"",
            options = listOf("go", "goes", "went", "going"),
            correctIndex = 2,
            explanation = "\"Yesterday\" geçmiş zaman belirtir, \"go\" fiilinin V2 hali \"went\"tir."
        ),
        QuizQuestion(
            courseId = "ing",
            unit = "The Internet",
            question = "\"She is ___ than her sister.\"",
            options = listOf("tall", "taller", "tallest", "more tall"),
            correctIndex = 1,
            explanation = "Kısa tek heceli sıfatlarda karşılaştırma (comparative) için \"-er than\" kullanılır: taller than."
        ),
        QuizQuestion(
            courseId = "din",
            unit = "Kader İnancı",
            question = "Kader inancı insanı hangi davranıştan uzak tutmalıdır?",
            options = listOf("Çalışmaktan", "Şükretmekten", "Sabretmekten", "Dua etmekten"),
            correctIndex = 0,
            explanation = "Kader inancı insanı çalışmaktan ve sorumluluk almaktan alıkoymaz; tembelliğe bahane yapılamaz."
        ),
        QuizQuestion(
            courseId = "din",
            unit = "Zekât ve Sadaka",
            question = "Aşağıdakilerden hangisine zekât verilmez?",
            options = listOf("Fakire", "Borçluya", "Kişinin kendi çocuğuna", "Yolda kalmışa"),
            correctIndex = 2,
            explanation = "Kişi bakmakla yükümlü olduğu anne-babasına, eşine ve çocuklarına zekât veremez."
        )
    )

    val videoTopics: Map<String, List<String>> = mapOf(
        "mat|Çarpanlar ve Katlar" to listOf("Pozitif Çarpanlar", "Asal Çarpanlara Ayırma", "EBOB ve EKOK"),
        "mat|Üslü İfadeler" to listOf("Üslü Sayılarda İşlemler"),
        "mat|Kareköklü İfadeler" to listOf("Karekök İşlemleri"),
        "mat|Veri Analizi" to listOf("Ortalama, Ortanca, Tepe Değer"),
        "mat|Olasılık" to listOf("Olasılık Hesaplama"),
        "mat|Cebirsel İfadeler" to listOf("Cebirsel Özdeşlikler"),
        "mat|Doğrusal Denklemler" to listOf("Doğrunun Eğimi ve Denklemi"),
        "mat|Eşitsizlikler" to listOf("Eşitsizliklerin Çözümü"),
        "mat|Üçgenler" to listOf("Üçgende Açı ve Kenar İlişkileri"),
        "mat|Dönüşüm Geometrisi" to listOf("Öteleme ve Yansıma"),
        "mat|Geometrik Cisimler" to listOf("Prizmalarda Hacim"),
        "fen|Mevsimler ve İklim" to listOf("Mevsimlerin Oluşumu"),
        "fen|DNA ve Genetik Kod" to listOf("DNA'dan Kromozoma"),
        "fen|Basınç" to listOf("Katı Basıncı"),
        "fen|Madde ve Endüstri" to listOf("Asit-Baz ve pH"),
        "fen|Basit Makineler" to listOf("Kaldıraç ve Makaralar"),
        "fen|Enerji Dönüşümleri" to listOf("Enerjinin Korunumu"),
        "fen|Elektrik Yükleri" to listOf("Elektriklenme ve Yükler"),
        "tur|Fiilimsiler" to listOf("Fiilimsi Çeşitleri"),
        "tur|Cümlenin Ögeleri" to listOf("Cümlenin Ögelerini Bulma"),
        "tur|Cümle Türleri" to listOf("Anlamına ve Yüklemine Göre Cümleler"),
        "tur|Fiillerde Çatı" to listOf("Fiillerde Çatı (Edilgen, Dönüşlü, İşteş)"),
        "tur|Paragraf" to listOf("Paragrafta Ana Düşünce ve Yapı"),
        "tur|Yazım ve Noktalama" to listOf("de/da ve Kesme İşareti Kuralları"),
        "tur|Söz Sanatları" to listOf("Temel Söz Sanatları"),
        "ink|Bir Kahraman Doğuyor" to listOf("Mustafa Kemal'in Çocukluğu ve Gençliği"),
        "ink|Milli Uyanış" to listOf("Kongreler Dönemi"),
        "ink|Milli Bir Destan" to listOf("Kurtuluş Savaşı Cepheleri"),
        "ink|Atatürkçülük" to listOf("Atatürk İlkeleri"),
        "ink|Demokratikleşme Çabaları" to listOf("Çok Partili Hayata Geçiş"),
        "ink|Dış Politika" to listOf("Atatürk Dönemi Dış Politikası"),
        "din|Kader İnancı" to listOf("Kaza ve Kader"),
        "din|Zekât ve Sadaka" to listOf("Zekât ve Sadaka Kavramları"),
        "din|Din ve Hayat" to listOf("Din ve Ahlaki Değerler"),
        "din|Hz. Muhammed'in Örnekliği" to listOf("Hz. Muhammed'in Güzel Ahlakı"),
        "din|Kur'an-ı Kerim ve Yorumu" to listOf("Tefsir ve Meal Kavramları"),
        "ing|Adventures" to listOf("Present Perfect Tense")
    )

    fun getVideoLesson(courseId: String, unit: String, topic: String): VideoLesson {
        val key = "$courseId|$unit|$topic"
        return builtInVideoLessons[key] ?: VideoLesson(
            name = topic,
            sections = listOf(
                VideoSection(
                    key = "giris",
                    title = "Konuya Giriş",
                    script = listOf(
                        "Merhaba sevgili öğrencim! Ben Prof. Dr. Yusuf Salih. Bugün seninle $topic konusunu baştan sona, klinik bir titizlikle öğreneceğiz.",
                        "Bu konu LGS'de kritik bir önem taşır. Hazırsan arkana yaslan, dersimiz başlıyor!"
                    ),
                    board = listOf(
                        listOf(BoardStep.Header(topic)),
                        listOf(BoardStep.Paragraph("LGS 2027 Klinik Üssü Ders Paneli"))
                    )
                ),
                VideoSection(
                    key = "temel",
                    title = "Temel Kavramlar",
                    script = listOf(
                        "$topic konusunda dikkat etmen gereken en temel kural, mantığı kavramaktır. Ezberlemek yerine soruya adım adım yaklaşmalısın.",
                        "Tahtada gördüğün formül ve püf noktaları defterine not almayı unutma."
                    ),
                    board = listOf(
                        listOf(BoardStep.Header("Temel Püf Noktaları")),
                        listOf(BoardStep.ItemList(listOf("Formülü tam anla", "Öncülleri dikkatle oku", "Hata yapmaktan korkma")))
                    )
                ),
                VideoSection(
                    key = "ozet",
                    title = "Konu Özeti",
                    script = listOf("Harika bir odaklanma sergiledin! Şimdi bu konunun özetini gözden geçirip teste geçebilirsin."),
                    board = listOf(listOf(BoardStep.Header("Özet"))),
                    summary = listOf(
                        "$topic konusunun temel prensiplerini kavradın.",
                        "LGS sınavında bu tarz sorularda acele etmeden işlem yap."
                    )
                ),
                VideoSection(
                    key = "sonraki",
                    title = "Tebrikler",
                    script = listOf("Tebrikler! $topic konusunu başarıyla tamamladın. Şimdi test çözerek bilgini pekiştir!"),
                    board = listOf(listOf(BoardStep.Header("Tebrikler! 🎯")))
                )
            )
        )
    }

    val builtInVideoLessons: Map<String, VideoLesson> = mapOf(
        "mat|Çarpanlar ve Katlar|Pozitif Çarpanlar" to VideoLesson(
            name = "Pozitif Çarpanlar",
            sections = listOf(
                VideoSection(
                    key = "giris",
                    title = "Konuya Giriş",
                    script = listOf(
                        "Merhaba! Ben Prof. Salih. Bugün seninle \"Pozitif Çarpanlar\" konusunu baştan sona öğreneceğiz.",
                        "Bu konu LGS'de çok sık karşına çıkıyor, çünkü EBOB, EKOK ve asal çarpanlara ayırma gibi pek çok konunun temelini oluşturuyor.",
                        "Dersin sonunda bir sayının pozitif çarpanlarını nasıl bulacağını ve kaç tane çarpanı olduğunu tek tek saymadan nasıl hesaplayacağını öğrenmiş olacaksın. Hazırsan başlıyoruz!"
                    ),
                    board = listOf(
                        listOf(BoardStep.Header("Pozitif Çarpanlar")),
                        listOf(BoardStep.ItemList(listOf("EBOB'un temeli", "EKOK'un temeli", "Asal çarpanlara ayırmanın temeli")))
                    )
                ),
                VideoSection(
                    key = "temel",
                    title = "Temel Kavram",
                    script = listOf(
                        "Bir sayının çarpanı, o sayıyı kalansız bölen sayıdır. Yani bir sayıyı tam olarak bölebiliyorsak, bölen sayı onun çarpanıdır.",
                        "Örnek olarak 12 sayısını ele alalım. 12'yi hangi sayılar tam böler, birlikte bulalım.",
                        "12'yi 1'e böldüğümüzde sonuç 12'dir. Demek ki 1 ve 12 birer çarpandır.",
                        "12'yi 2'ye böldüğümüzde 6 eder. O zaman 2 ve 6 da çarpan.",
                        "12'yi 3'e böldüğümüzde 4 eder. 3 ve 4 de bir çarpan çifti.",
                        "12'nin pozitif çarpanları: 1, 2, 3, 4, 6 ve 12'dir. Toplamda 6 tane çarpanı var.",
                        "Peki tek tek saymadan nasıl buluruz? 12 = 2² × 3¹ olduğuna göre üslere 1 ekleyip çarparız: (2+1) × (1+1) = 3 × 2 = 6 çarpan!"
                    ),
                    board = listOf(
                        listOf(BoardStep.Paragraph("Çarpan = Bir sayıyı kalansız bölen sayı")),
                        listOf(BoardStep.Header("12'nin Çarpanları")),
                        listOf(BoardStep.PairEq(1, 12)),
                        listOf(BoardStep.PairEq(2, 6)),
                        listOf(BoardStep.PairEq(3, 4)),
                        listOf(BoardStep.ItemList(listOf("1", "2", "3", "4", "6", "12"))),
                        listOf(BoardStep.Equation("12 = 2² × 3¹")),
                        listOf(BoardStep.Equation("(2+1) × (1+1) = 3 × 2 = 6"))
                    )
                ),
                VideoSection(
                    key = "hatalar",
                    title = "Sık Yapılan Hatalar",
                    script = listOf(
                        "Birinci hata: Çarpanları sayarken 1'i ve sayının kendisini unutmak. 18'in çarpanlarında 1 ve 18 mutlaka olmalı!",
                        "İkinci hata: Asal çarpanlara ayırdıktan sonra üsleri toplamak. Üsleri toplamayacağız, birer ekleyip çarpacağız: (2+1)×(1+1)=6."
                    ),
                    board = listOf(
                        listOf(BoardStep.Header("Sık Yapılan Hatalar")),
                        listOf(BoardStep.Paragraph("❌ Yanlış: 18'in çarpanları 2, 3, 6, 9")),
                        listOf(BoardStep.Paragraph("✓ Doğru: 1, 2, 3, 6, 9, 18")),
                        listOf(BoardStep.Paragraph("❌ Yanlış: 12 = 2²×3 için 2+1=3")),
                        listOf(BoardStep.Paragraph("✓ Doğru: (2+1)×(1+1) = 6"))
                    ),
                    mistakes = listOf(
                        "18'in çarpanları 2, 3, 6, 9'dur (1 ve 18 unutuldu)" to "1 ve sayının kendisi her zaman çarpandır.",
                        "12=2²×3 için çarpan sayısı 2+1=3 sanılır" to "Üsler toplanmaz, (üs+1) değerleri ÇARPILIR: (2+1)×(1+1)=6"
                    )
                ),
                VideoSection(
                    key = "kontrol",
                    title = "Mini Kontrol",
                    script = listOf("Şimdi öğrendiklerini birkaç soruyla kontrol edelim. Hazırsan başlayalım!"),
                    board = listOf(listOf(BoardStep.Header("Mini Kontrol"))),
                    quiz = listOf(
                        MiniQuizItem("20 sayısının pozitif çarpan sayısı kaçtır?", listOf("4", "6", "8", "10"), 1, "20 = 2² × 5¹ → (2+1)(1+1) = 6"),
                        MiniQuizItem("1 sayısı her sayının çarpanı mıdır?", listOf("Evet", "Hayır"), 0, "Evet, 1 her doğal sayıyı kalansız böler."),
                        MiniQuizItem("2³ × 3² ifadesinin pozitif çarpan sayısı kaçtır?", listOf("9", "10", "12", "15"), 2, "(3+1) × (2+1) = 4 × 3 = 12")
                    )
                ),
                VideoSection(
                    key = "ozet",
                    title = "Konu Özeti",
                    script = listOf("Harika iş çıkardın! Şimdi bu konunun en önemli noktalarını birlikte tekrar edelim."),
                    board = listOf(listOf(BoardStep.Header("Konu Özeti"))),
                    summary = listOf(
                        "Bir sayıyı kalansız bölen her sayı, onun çarpanıdır.",
                        "Çarpanlar her zaman 1 ile başlar, sayının kendisiyle biter.",
                        "Çarpan çiftlerini ararken kareköke kadar arayıp durabiliriz.",
                        "Çarpan sayısını bulmak için: asal çarpanlarına ayır, her üsse 1 ekleyip çarp."
                    )
                ),
                VideoSection(
                    key = "sonraki",
                    title = "Sonraki Adım",
                    script = listOf("Tebrikler, Pozitif Çarpanlar konusunu tamamladın! Şimdi testle pekiştirebilirsin."),
                    board = listOf(listOf(BoardStep.Header("Tebrikler! 🎯")))
                )
            )
        ),
        "mat|Çarpanlar ve Katlar|EBOB ve EKOK" to VideoLesson(
            name = "EBOB ve EKOK",
            sections = listOf(
                VideoSection(
                    key = "giris",
                    title = "Konuya Giriş",
                    script = listOf(
                        "Şimdi EBOB ve EKOK konusuna geçiyoruz. LGS'de problemlerin vazgeçilmezidir.",
                        "EBOB: En Büyük Ortak Bölen. EKOK: En Küçük Ortak Kat."
                    ),
                    board = listOf(
                        listOf(BoardStep.Header("EBOB ve EKOK")),
                        listOf(BoardStep.ItemList(listOf("EBOB: En Büyük Ortak Bölen", "EKOK: En Küçük Ortak Kat")))
                    )
                ),
                VideoSection(
                    key = "temel",
                    title = "Temel Kural",
                    script = listOf(
                        "EBOB'u bulmak için sayıları asal çarpanlarına ayırırız ve ORTAK olan asalları KÜÇÜK üsleriyle çarparız.",
                        "EKOK için ise TÜM asalları BÜYÜK üsleriyle çarparız.",
                        "12 = 2² × 3¹ ve 18 = 2¹ × 3² olduğuna göre:",
                        "EBOB(12, 18) = 2¹ × 3¹ = 6.",
                        "EKOK(12, 18) = 2² × 3² = 4 × 9 = 36."
                    ),
                    board = listOf(
                        listOf(BoardStep.Paragraph("EBOB = Ortak asalların EN KÜÇÜK üsleri")),
                        listOf(BoardStep.Equation("12 = 2² × 3¹")),
                        listOf(BoardStep.Equation("18 = 2¹ × 3²")),
                        listOf(BoardStep.Equation("EBOB(12,18) = 2¹ × 3¹ = 6")),
                        listOf(BoardStep.Paragraph("EKOK = Tüm asalların EN BÜYÜK üsleri")),
                        listOf(BoardStep.Equation("EKOK(12,18) = 2² × 3² = 36"))
                    )
                ),
                VideoSection(
                    key = "kontrol",
                    title = "Mini Kontrol",
                    script = listOf("Hadi mini kontrolü çözelim."),
                    board = listOf(listOf(BoardStep.Header("Mini Kontrol"))),
                    quiz = listOf(
                        MiniQuizItem("EBOB(4, 6) kaçtır?", listOf("1", "2", "4", "12"), 1, "Ortak asal 2¹ = 2."),
                        MiniQuizItem("EKOK(4, 6) kaçtır?", listOf("2", "6", "12", "24"), 2, "2² × 3 = 12.")
                    )
                ),
                VideoSection(
                    key = "ozet",
                    title = "Konu Özeti",
                    script = listOf("Özetle EBOB küçük, EKOK büyük düşünülür. EBOB ortak küçük üs, EKOK tüm büyük üs."),
                    board = listOf(listOf(BoardStep.Header("Özet"))),
                    summary = listOf(
                        "EBOB = ortak asallar × en küçük üsler.",
                        "EKOK = tüm asallar × en büyük üsler."
                    )
                ),
                VideoSection(
                    key = "sonraki",
                    title = "Sonraki Adım",
                    script = listOf("Tebrikler! EBOB ve EKOK konusunu tamamladın."),
                    board = listOf(listOf(BoardStep.Header("Tebrikler! 🎯")))
                )
            )
        )
    )

    val studyTactics: List<Pair<String, String>> = listOf(
        "1. Soruyu ikiye böl" to "Önce sorunun sonundaki asıl soru cümlesini oku. Neyin istendiğini bilmeden metne dalma.",
        "2. Metni işaretle" to "Sayıları, birimleri, tarihleri ve \"en az / en çok / hariç\" gibi anahtar kelimelerin altını çizerek oku.",
        "3. Verileri tabloya dök" to "Uzun hikâyeyi kısa bir liste, tablo ya da şekle çevir. Fen ve matematikte küçük bir çizim çoğu zaman çözümdür.",
        "4. Bilinenleri ve bilinmeyeni yaz" to "Elindeki verileri \"Verilen\" başlığıyla, istenenleri \"İstenen\" başlığıyla yaz. Formül burada kendiliğinden ortaya çıkar.",
        "5. Şıkları kullan" to "Şıklara bakarak tahmin aralığı belirle. Birim, büyüklük ve işaret kontrolüyle yanlış şıkları ele.",
        "6. Sonucu kontrol et" to "Bulduğun değeri soruya geri koy. Mantıklı mı? Birimi doğru mu? Sorulan şeyi mi buldun?",
        "7. Türkçe / paragraf için" to "Ana fikir sorusunda son cümleye, yardımcı düşünce sorusunda detay cümlelerine odaklan. Ön yargıyla değil metinle cevapla."
    )

    val motivationQuotes: List<String> = listOf(
        "Bugünkü emek, LGS günü gülümsemendir.",
        "Her doğru soru, hedefine attığın bir adımdır.",
        "Yanlışlar seni yenmez; öğretir. Yanlış defterin senin en iyi hocan.",
        "Küçük ama her gün yapılan çalışma, büyük ama düzensiz çalışmayı yener.",
        "Vazgeçmek için değil, başlamak için bir sebep bul.",
        "Netlerin bugün düşük olabilir; önemli olan grafiğin yönü."
    )

    val successfulHabitTips: List<String> = listOf(
        "Ders çalışırken telefonu başka odaya bırak.",
        "Her gün yanlış defterini 10 dakika gözden geçir.",
        "Denemeden sonra analiz, denemenin kendisi kadar önemlidir.",
        "Uykunu ihmal etme: hafıza uykuda pekişir.",
        "Önce kolay soruları çöz, zor soruda takılıp kalma; işaretle ve dön.",
        "Konuyu bitirince kendi cümlelerinle bir kartla özetle."
    )

    val badgeDefinitions: List<BadgeItem> = listOf(
        BadgeItem("first_step", "🌱", "İlk Adım", "1 konu bitir"),
        BadgeItem("math_master", "📐", "Matematik Canavarı", "Matematikte 3 konu bitir"),
        BadgeItem("sci_explorer", "🧪", "Fen Kaşifi", "Fende 3 konu bitir"),
        BadgeItem("q_hunter", "🎯", "Soru Avcısı", "10 doğru cevap ver"),
        BadgeItem("card_pro", "🎴", "Kart Ustası", "20 kart çevir"),
        BadgeItem("mistake_hunter", "🕵️", "Hata Avcısı", "3 yanlışı çözüldü işaretle"),
        BadgeItem("focus_champ", "⏱️", "Odak Şampiyonu", "3 pomodoro tamamla"),
        BadgeItem("lgs_star", "🏆", "LGS Derece Adayı", "12 konu bitir"),
        BadgeItem("streak_3", "🔥", "3 Günlük Seri", "3 gün üst üste çalış"),
        BadgeItem("streak_7", "🌟", "7 Günlük Seri", "7 gün üst üste çalış"),
        BadgeItem("time_master", "⏳", "Saat Avcısı", "Toplam 300 dk çalış"),
        BadgeItem("q_machine", "⚡", "Soru Makinesi", "50 soru çöz"),
        BadgeItem("trial_surgeon", "🩺", "Deneme Cerrahı", "3 deneme dosyası aç"),
        BadgeItem("q_collector", "📸", "Soru Koleksiyoncusu", "3 kendi sorunu ekle"),
        BadgeItem("srs_master", "🗃️", "Tekrar Ustası", "30 akıllı tekrar yap"),
        BadgeItem("green_zone", "🟢", "Yeşil Bölge", "3 konuyu sağlıklı yap"),
        BadgeItem("checkup_regular", "📋", "Check-up Düzenlisi", "Haftalık raporu aç"),
        BadgeItem("safe_archive", "💾", "Güvenli Arşiv", "İlk yedeğini al"),
        // Secret easter-egg badges
        BadgeItem("konami", "🩺", "Gizli Doktor", "Gizli dokunuşu keşfet", isSecret = true),
        BadgeItem("steto", "🩻", "Stetoskop Dinleyicisi", "Klinik logosuna 7 kez dokun", isSecret = true),
        BadgeItem("owl", "🦉", "Gece Baykuşu", "Gece 23:00 - 04:00 arası soru çöz", isSecret = true),
        BadgeItem("bird", "🐦", "Erken Kuş", "Sabah 04:00 - 07:00 arası çalış", isSecret = true),
        BadgeItem("sun", "☀️", "Pazar Gönüllüsü", "Pazar günü soru çöz", isSecret = true),
        BadgeItem("phoenix", "🔥", "Yeniden Doğuş", "Kırmızı olan bir konuyu yeşile çevir", isSecret = true),
        BadgeItem("gem", "💎", "Kusursuz Muayene", "Günlük Check-up'ta 5/5 tam yap", isSecret = true)
    )
}
