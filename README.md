[ÖDEV RAPORU.pdf](https://github.com/Karatasdilara/PlantApp/files/14145115/DILARAKARATASSONRAPOR.pdf)

#Proje Hakkında:

Proje, bitkileri tanıma ve bu bitkilerle ilgili detaylı bilgi sunma amacı taşıyan bir mobil uygulamadır. Uygulama, kullanıcılara kamera veya galeriden yüklenmiş fotoğraflar aracılığıyla bitki tanıma imkanı sunar. Kullanıcılar, tanınan bitkileri kendi özel listelerine ekleyebilir ve bitkileri listeden silebilir. Ayrıca, bitkilerle ilgili bildirim alabilirler. Proje, Android Studio'da Kotlin programlama dili kullanılarak geliştirilmektedir ve TensorFlow derin öğrenme kütüphanesi, bitki tanıma işlevselliğini sağlamak için entegre edilmektedir.

#Yöntem ve Teknikler:

Firebase Platformu:

Firebase Authentication: Kullanıcı kimlik doğrulama işlemleri için kullanılmıştır.

Cloud Firestore: Kullanıcı bilgileri, bitki listeleri ve bitki özellikleri gibi veriler için NoSQL veritabanı olarak kullanılmıştır.

Firebase Cloud Messaging (FCM): Kullanıcılara sulama hatırlatmaları göndermek için kullanılmıştır.

Android Jetpack Kütüphaneleri:

ViewModel ve LiveData: Veri bağlama işlemleri ve yaşam döngüsü bilgilerinin yönetimi için kullanılmıştır.

Navigation Component: Fragment yönetimi ve gezinme işlemleri için kullanılmıştır.

RecyclerView: Bitki listeleri ve diğer listelerin oluşturulması için kullanılmıştır.

TensorFlow Lite:

Bitki tanıma özelliği için kullanılmıştır.

#Proje Mimarisi:

Bu uygulamada MVVM mimarisini kullanmak uygun olacaktır.
MVVM (Model-View-ViewModel), yazılım geliştirme sürecinde kullanılan bir mimari desen veya yapıdır. Temel olarak üç ana bileşen içerir:

1.Model (Model): Verilerin ve iş mantığının bulunduğu kısımdır. Veri işleme, depolama ve iş mantığı bu katmanda yer alır.

2.View (Görünüm): Kullanıcı arayüzünü temsil eder. Kullanıcıya gösterilen grafik arayüz, düğmeler, formlar gibi bileşenler bu katmanda bulunur.

3.ViewModel: Model ve View arasındaki iletişimi sağlayan ara katmandır. Kullanıcı arayüzündeki görsel elemanlarla etkileşim kurar, gerekli verileri Model'den alır, bunları işler ve View'a sunar.

#Proje Görselleri

![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/65992e6f-6df3-44f8-9445-d424e74d9952) ![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/a0caae3c-3a43-431a-b761-cfd3ba95d670)

![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/072f8d72-4a53-4e1d-b353-d7bd980a38f3) ![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/250f11e9-c019-462c-8798-952251d509cc)


![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/5e0d1b88-6ca7-4604-b170-8cc708358d9b) ![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/922008e5-4e57-4ad9-949c-e7c641aa773b)


![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/17ca192b-b8c0-404c-bd98-25376e692ff4) ![image](https://github.com/Karatasdilara/PlantApp/assets/116079552/070ec0ad-ec7b-4b5d-8476-84caf400d2c7)







