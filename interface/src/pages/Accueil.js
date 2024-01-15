import React from 'react';
import { NavLink } from 'react-router-dom';
import DisableBackgroundWiki from '../components/DisableBackgroundWiki';
import Header from '../components/Header';

const Home = () => {
    return (
        <div>
            <DisableBackgroundWiki />
            <Header/>
            <div class="padded">
                <h2>Bienvenue dans WekinGames, la référence du wiki jeux vidéo </h2>
                <NavLink to="/allWikis">
                    <button class="text-small">Liste des wikis</button>
                </NavLink>
                <p>Créez votre propre wiki dès maintenant !</p>
                <NavLink to="/createWiki">
                    <button class="text-small">Créer votre wiki</button>
                </NavLink>
            </div>
        </div>
    );
};

export default Home;
