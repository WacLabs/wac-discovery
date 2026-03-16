# Release Process — wac-discovery

Checklist để publish version mới lên Maven Central.

## Pre-release

1. **Merge tất cả PR** cần thiết vào `main`
2. **Pull latest code:**
   ```bash
   git pull origin main
   ```
3. **Verify build** trước khi release:
   ```bash
   ./gradlew :wac-discovery:build
   ```

## Release

4. **Bump version** trong `gradle.properties`:
   ```properties
   VERSION_NAME=x.y.z
   ```

5. **Update README.md** — cập nhật version trong Installation section:
   ```kotlin
   implementation("io.github.waclabs:wac-discovery:x.y.z")
   ```

6. **Commit version bump + README:**
   ```bash
   git add gradle.properties README.md
   git commit -m "chore: release vX.Y.Z"
   ```

7. **Create git tag:**
   ```bash
   git tag -a vX.Y.Z -m "brief description of changes"
   ```

8. **Push commit + tag:**
   ```bash
   git push origin main
   git push origin vX.Y.Z
   ```

9. **Publish to Maven Central:**
   ```bash
   ./gradlew :wac-discovery:publishAllPublicationsToMavenCentralRepository
   ```

## Post-release

10. **Verify on Maven Central** — artifacts thường mất 10-30 phút để sync:
    - https://central.sonatype.com/artifact/io.github.waclabs/wac-discovery

11. **Create GitHub Release** (optional):
    ```bash
    gh release create vX.Y.Z --title "vX.Y.Z" --notes "changelog here"
    ```
