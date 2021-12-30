package extension

import java.io.File

/**
 * desc: string 相关扩展
 * author: teprinciple on 2020/3/27.
 */

/**
 * 根据文件路径删除文件
 */
fun String?.deleteFile() {
    kotlin.runCatching {
        val file = File(this ?: "")
        (file.isFile).yes {
            file.delete()
            log("delete:$this")
        }
    }.onFailure {
        log(it.message)
    }
}