
#!/usr/bin/env bash
if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    openssl aes-256-cbc -K $encrypted_7167b1c3cac1_key -iv $encrypted_7167b1c3cac1_iv  -in .travis/codesigning.asc.enc -out .travis/codesigning.asc -d

    gpg --fast-import .travis/codesigning.asc
fi
