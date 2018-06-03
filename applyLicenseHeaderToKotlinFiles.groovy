/*
 * MIT License
 *
 * Copyright (c) 2018 Alexander Widar
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/* =================================================================================
 * Welcome to my ugly hack script which I had to make because the hierynomus gradle
 * licence plugin doesn't do Kotlin files for some reason. Might remove it if that
 * changes.
 *
 * https://github.com/hierynomus/license-gradle-plugin/issues/155
 *
 * =================================================================================
 */

package se.alexanderwidar
import groovy.io.FileVisitResult
import static groovy.io.FileType.FILES

def templateEngine = new groovy.text.SimpleTemplateEngine()
def templateBinding = [name: "Alexander Widar", year: "2018"]

String headerText = new File("mit.header").text
String header = templateEngine.createTemplate(createHeader(headerText)).make(templateBinding).toString()

File src = new File("steerAndPut/src")


src.traverse(
        type: FILES,
        nameFilter: ~/.*\.kt/
) { file ->
    println "Processing $file"

    String fileText = file.text
    println "Existing header: ${hasHeader(fileText)}"
    if (hasHeader(fileText)) {
        fileText = removeHeader(fileText)
    }

    fileText = applyHeader(header, fileText)
    file.text = fileText
}

def hasHeader(String fileContents) {
    return fileContents.startsWith("/*")
}

def applyHeader(String header, String fileContents) {
    return header + fileContents
}

def createHeader(String headerText) {
    StringBuilder sb = new StringBuilder()
    sb << "/*\n"
    headerText.eachLine {
        sb << " * $it\n"
    }
    sb << " */\n"
    return sb.toString()
}

def removeHeader(String fileContents) {
    int lastComment = fileContents.indexOf("*/")
    return fileContents.drop(lastComment + 3)
}