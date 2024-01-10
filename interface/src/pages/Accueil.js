import React from 'react';
import { NavLink } from 'react-router-dom';
import DisableBackgroundWiki from '../components/DisableBackgroundWiki';
import Header from '../components/Header';

const Home = () => {
    return (
        <div>
            <DisableBackgroundWiki />
            <Header />
            <h4>Bienvenue dans WekinGames, la référence du wiki jeux vidéo </h4>
            <NavLink to="/allWikis">
                <button class="text-small">Liste des wikis</button>
            </NavLink>
            <h2>Créez votre propre wiki dès maintenant !</h2>
            <NavLink to="/createWiki">
                <button class="text-small">Créer votre wiki</button>
            </NavLink>
        </div>
    );
};

export default Home;
