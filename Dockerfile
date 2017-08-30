FROM clojure:lein-2.7.1-alpine

# cider repl fix
RUN mkdir -p /Users/m_kuzmin/projects/github \
    && ln -s /usr/src/app /Users/m_kuzmin/projects/github/form
