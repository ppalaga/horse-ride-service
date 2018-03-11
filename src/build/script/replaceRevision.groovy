import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

def replaceRevision(path, revision) {
    String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    content = content.replace("@revision@", revision);
    Files.write(path, content.getBytes(StandardCharsets.UTF_8));
}

Repository repo = null;
Git git = null;
try {
    repo = new FileRepositoryBuilder().setGitDir(new File("${basedir}/.git")).setMustExist(true)
        .build();
    git = new Git(repo)
    Iterable<RevCommit> log = git.log().call();
    Iterator<RevCommit> it = log.iterator();
    if (it.hasNext()) {
        RevCommit rev = it.next();
        String revisionText = rev.getId().getName().substring(0, 8) + " "+ rev.getFullMessage();
        replaceRevision(Paths.get("${basedir}/target/classes/static/index.html"), revisionText);
        replaceRevision(Paths.get("${basedir}/target/classes/application.properties"), revisionText);
    }
} finally {
    if (git != null) {
        git.close();
    }
    if (repo != null) {
        repo.close();
    }
}

