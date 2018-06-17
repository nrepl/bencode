(defproject nrepl/bencode "1.0.0"
  :description "A netstring and bencode implementation for Clojure."
  :url "https://github.com/nrepl/bencode"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]

  :aliases {"bump-version" ["change" "version" "leiningen.release/bump-version"]}

  :release-tasks [["vcs" "assert-committed"]
                  ["bump-version" "release"]
                  ["vcs" "commit" "Release %s"]
                  ["vcs" "tag" "v" "--no-sign"]
                  ["bump-version"]
                  ["vcs" "commit" "Begin %s"]]

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_username
                                    :password :env/clojars_password
                                    :sign-releases false}]])
