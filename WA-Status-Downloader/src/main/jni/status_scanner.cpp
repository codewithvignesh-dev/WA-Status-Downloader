#include <jni.h>
#include <dirent.h>
#include <string>
#include <vector>

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_vp_statusdownloader_MainActivity_getStatusFiles(JNIEnv *env, jobject /* this */, jstring path) {
    const char *dirPath = env->GetStringUTFChars(path, nullptr);
    std::vector<std::string> mediaFiles;

    DIR *dir = opendir(dirPath);
    if (dir) {
        struct dirent *entry;
        while ((entry = readdir(dir)) != nullptr) {
            std::string name(entry->d_name);
            if (name.length() > 4 &&
                (name.substr(name.length() - 4) == ".mp4" || name.substr(name.length() - 4) == ".jpg")) {
                mediaFiles.emplace_back(std::string(dirPath) + "/" + name);
            }
        }
        closedir(dir);
    }

    jobjectArray result = env->NewObjectArray(mediaFiles.size(),
        env->FindClass("java/lang/String"), env->NewStringUTF(""));

    for (size_t i = 0; i < mediaFiles.size(); ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(mediaFiles[i].c_str()));
    }

    env->ReleaseStringUTFChars(path, dirPath);
    return result;
}
