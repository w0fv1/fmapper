plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

group = "dev.w0fv1"
version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
}

dependencies {

    implementation("com.google.auto.service:auto-service:1.1.1")
    annotationProcessor("com.google.auto.service:auto-service:1.1.1")
// https://mvnrepository.com/artifact/jakarta.persistence/jakarta.persistence-api
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
// https://mvnrepository.com/artifact/com.squareup/javapoet
    implementation("com.squareup:javapoet:1.13.0")


    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}


//import org.gradle.plugins.signing.Sign

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Hello Spring Lib")
                description.set("A Spring Boot starter that says hello.")
                url.set("https://github.com/w0fv1/fmapper")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("w0fv1")
                        name.set("w0fv1")
                        email.set("hi@w0fv1.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/w0fv1/fmapper.git")
                    developerConnection.set("scm:git:ssh://github.com/w0fv1/fmapper.git")
                    url.set("https://github.com/w0fv1/fmapper")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/w0fv1/fmapper")
            credentials {
                username = "w0fv1" // 你的 GitHub 用户名
                password = System.getProperty("gpr.token") // 从系统属性中读取 Token
            }
        }
    }
}

signing {
    // 如果在构建时手动输入密码，可以使用 `useGpgCmd()` 启用命令行 GPG
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
