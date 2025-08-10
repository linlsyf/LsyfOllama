curl http://localhost:11434/api/tags



=========
docker ps -a | grep ':11434->'


docker  stop  c10c40e38cb5 



docker run -d -e OLLAMA_HOST=0.0.0.0 -p 11434:11434 ollama/ollama

docker exec -it     ollama run qwen2.5-coder:0.5b

docker exec -it     ollama rm llama3.2



docker exec -it     ollama run qwen2.5-coder:0.5b

===
docker exec ollama ollama pull qwen2.5-coder:0.5b

docker exec ollama ollama run qwen2.5-coder:0.5b

ollama cp qwen2.5-coder:0.5b mqwen2.5-coder-0.5b


ollama rm mqwen2.5-coder-0.5b


docker exec -it   docker ollama run qwen2.5-coder:14b
===============

# 停止所有运行中的 Ollama 容器
docker stop $(docker ps -q --filter ancestor=ollama/ollama)

# 强制删除所有关联容器（包括已停止的）
docker rm -f $(docker ps -aq --filter ancestor=ollama/ollama)

docker exec -it          4ac951805169       ollama rm qwen2.5-coder:1.5b

docker exec  -it   4ac951805169   ollama  stop  qwen2.5-coder:0.5b

docker exec  -it   4ac951805169   ollama  run  qwen2.5-coder:0.5b

sync; echo 3 | sudo tee /proc/sys/vm/drop_caches  # 清理系统缓存