import os
import subprocess
import shutil
import configparser
import glob
import time
# import requests # 移除 requests 库

# --- 配置区域 ---
# 模组项目根目录 (脚本将放置在此目录下)
MOD_PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))

# 服务器根目录
SERVER_ROOT = r"C:\Users\邓旭东\Desktop\新建文件夹\minecraft_server"

# 服务器的mods文件夹路径
SERVER_MODS_DIR = r"C:\Users\邓旭东\Desktop\新建文件夹\minecraft_server\mods"

# 服务器启动脚本路径
SERVER_RUN_SCRIPT = os.path.join(SERVER_ROOT, "run.bat")

# 预期的本地 MySQL JDBC Mod 文件名 (根据用户提供的截图和放置位置)
LOCAL_MYSQL_JDBC_MOD_FILENAME = "mysql-jdbc-8.0.33+20230506-all.jar"
# --- 配置区域结束 ---

def run_command(command, cwd):
    """在指定目录下运行终端命令并检查错误"""
    print(f"正在执行命令: {' '.join(command)} 在目录: {cwd}")
    try:
        # 使用实时输出模式
        process = subprocess.Popen(
            command,
            cwd=cwd,
            shell=True,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            encoding='gbk',  # 使用GBK编码
            bufsize=1,
            universal_newlines=True
        )

        # 实时读取输出
        while True:
            output = process.stdout.readline()
            if output == '' and process.poll() is not None:
                break
            if output:
                print(output.strip())

        # 获取返回码
        return_code = process.poll()
        
        # 检查是否有错误输出
        error_output = process.stderr.read()
        if error_output:
            print("命令错误输出:")
            print(error_output)

        return return_code == 0
    except FileNotFoundError:
        print(f"错误: 找不到命令 {command[0]}。请确保它在您的PATH中或提供完整路径。")
        return False
    except Exception as e:
        print(f"执行命令时出错: {e}")
        return False

def get_mod_info(gradle_properties_path):
    """从 gradle.properties 文件中读取 mod_id 和 mod_version"""
    try:
        config = configparser.ConfigParser()
        # ConfigParser 默认不支持没有节头的ini文件，需要手动添加一个假节
        with open(gradle_properties_path, 'r', encoding='utf-8') as f:
            config.read_string('[DEFAULT]\n' + f.read())
        
        mod_id = config['DEFAULT'].get('mod_id')
        minecraft_version = config['DEFAULT'].get('minecraft_version')
        mod_version = config['DEFAULT'].get('mod_version')
        
        if not all([mod_id, minecraft_version, mod_version]):
            print("错误: 在 gradle.properties 中找不到 mod_id, minecraft_version 或 mod_version")
            return None, None
        
        # 模组文件名通常是 ${mod_id}-${minecraft_version}-${mod_version}.jar 或 ${mod_id}-${minecraft_version}.jar
        # 根据 gradle.properties 中的 base.archivesName 推断，文件名可能是 ${mod_id}-${minecraft_version}.jar
        mod_jar_filename = f"{mod_id}-{minecraft_version}.jar"
        # 如果需要包含版本号，可以使用这个：
        # mod_jar_filename = f"{mod_id}-{minecraft_version}-{mod_version}.jar"
        
        return mod_jar_filename, mod_id
    except Exception as e:
        print(f"读取配置文件时出错: {e}")
        return None, None

def check_and_clear_old_mod(mod_id):
    """检查并清除旧的模组文件"""
    print(f"正在检查模组目录: {SERVER_MODS_DIR}")
    try:
        # 确保目录存在
        if not os.path.exists(SERVER_MODS_DIR):
            os.makedirs(SERVER_MODS_DIR)
            print(f"创建了模组目录: {SERVER_MODS_DIR}")
            return True

        # 查找匹配的旧模组文件
        old_mod_pattern = os.path.join(SERVER_MODS_DIR, f"{mod_id}-*.jar")
        old_mods = glob.glob(old_mod_pattern)
        
        if old_mods:
            print(f"找到 {len(old_mods)} 个旧模组文件:")
            for old_mod in old_mods:
                print(f"  - {os.path.basename(old_mod)}")
                try:
                    os.unlink(old_mod)
                    print(f"已删除: {os.path.basename(old_mod)}")
                except Exception as e:
                    print(f"删除文件时出错 {old_mod}: {e}")
                    return False
        else:
            print("未找到旧的模组文件，将直接复制新文件")
        
        return True
    except Exception as e:
        print(f"检查模组目录时出错: {e}")
        return False

def find_mod_jar(build_dir, mod_id):
    """在构建目录中查找模组JAR文件"""
    print(f"正在查找模组JAR文件...")
    # 尝试多种可能的文件名模式
    patterns = [
        f"{mod_id}-1.20.1-1.0.0.jar",  # 完整版本号
        f"{mod_id}-1.20.1-*.jar",      # 带版本号的特定版本
        f"{mod_id}-1.20.1.jar",        # 不带版本号的特定版本
        f"{mod_id}-*.jar"              # 匹配任何版本
    ]
    
    for pattern in patterns:
        matches = glob.glob(os.path.join(build_dir, "libs", pattern))
        if matches:
            # 按修改时间排序，获取最新的文件
            latest_jar = max(matches, key=os.path.getmtime)
            print(f"找到模组JAR文件: {os.path.basename(latest_jar)}")
            return latest_jar
    
    return None

def main():
    print("=== 开始模组自动化流程 ===")

    print("\n[步骤 1] 读取模组信息")
    gradle_properties_path = os.path.join(MOD_PROJECT_ROOT, "gradle.properties")
    mod_jar_filename, mod_id = get_mod_info(gradle_properties_path)

    if not mod_jar_filename:
        return

    print("\n[步骤 2] 清理并构建模组")
    if not run_command(["gradlew", "clean", "build"], MOD_PROJECT_ROOT):
        print("构建失败，退出程序")
        return

    print("\n[步骤 3] 检查并清理旧模组文件")
    if not check_and_clear_old_mod(mod_id):
        print("清理旧模组文件失败，退出程序")
        return

    print("\n[步骤 4] 检查并部署 MySQL JDBC JAR")
    local_mysql_jdbc_mod_path = os.path.join(MOD_PROJECT_ROOT, LOCAL_MYSQL_JDBC_MOD_FILENAME)
    destination_mysql_jdbc_mod_path = os.path.join(SERVER_MODS_DIR, LOCAL_MYSQL_JDBC_MOD_FILENAME)

    if os.path.exists(local_mysql_jdbc_mod_path):
        print(f"在模组项目根目录找到本地 MySQL JDBC JAR: {LOCAL_MYSQL_JDBC_MOD_FILENAME}")
        try:
            # 检查目标位置是否已经存在同名文件，如果存在则删除
            if os.path.exists(destination_mysql_jdbc_mod_path):
                print(f"目标 mods 目录已存在同名文件，正在删除: {LOCAL_MYSQL_JDBC_MOD_FILENAME}")
                os.unlink(destination_mysql_jdbc_mod_path)
                
            shutil.copy2(local_mysql_jdbc_mod_path, destination_mysql_jdbc_mod_path)
            print(f"成功复制 {LOCAL_MYSQL_JDBC_MOD_FILENAME} 到 {SERVER_MODS_DIR}")
        except FileNotFoundError:
            print(f"错误: 找不到目标服务器模组目录: {SERVER_MODS_DIR}")
            return # 无法复制则退出
        except Exception as e:
            print(f"复制文件时出错: {e}")
            return # 复制失败则退出
    else:
        print(f"未在模组项目根目录找到预期的本地 MySQL JDBC JAR 文件: {LOCAL_MYSQL_JDBC_MOD_FILENAME}")
        print("请将下载的 MySQL JDBC JAR 文件放置在模组项目根目录，并确保文件名为 {LOCAL_MYSQL_JDBC_MOD_FILENAME}。")
        # 在没有找到本地文件的情况下，停止后续部署以避免运行时错误
        return

    print("\n[步骤 5] 复制新模组文件")
    build_dir = os.path.join(MOD_PROJECT_ROOT, "build")
    source_jar_path = find_mod_jar(build_dir, mod_id)
    
    if not source_jar_path:
        print("错误: 找不到构建的JAR文件")
        print("请检查 build.gradle 和 gradle.properties 中的 archivesName 配置")
        return

    destination_jar_path = os.path.join(SERVER_MODS_DIR, os.path.basename(source_jar_path))

    try:
        shutil.copy2(source_jar_path, destination_jar_path)
        print(f"成功复制 {os.path.basename(source_jar_path)} 到 {SERVER_MODS_DIR}")
    except FileNotFoundError:
        print(f"错误: 找不到目标模组目录: {SERVER_MODS_DIR}")
        return
    except Exception as e:
        print(f"复制文件时出错: {e}")
        return

    print("\n=== 自动化流程完成 ===")
    print("模组和依赖已成功部署到服务器目录，您可以手动启动服务器了")

if __name__ == "__main__":
    # 检查所需的路径是否存在
    if not os.path.exists(MOD_PROJECT_ROOT):
         print(f"错误: 找不到模组项目目录: {MOD_PROJECT_ROOT}")
    elif not os.path.exists(SERVER_ROOT):
        print(f"错误: 找不到服务器目录: {SERVER_ROOT}")
    elif not os.path.exists(SERVER_MODS_DIR):
        print(f"错误: 找不到服务器模组目录: {SERVER_MODS_DIR}")
    else:
        main() 