import axios from 'axios';
import React, { useState } from 'react';
import { Link } from "react-router-dom";
import { API_URL } from '../config';

function ConnexionAuCompte(){
    const[name, setName]=useState('');
    const[password, setPassword]=useState('');

    const connectAccount = (name, password,dnaissance,email) =>{
        const data={
            pseudo: name,
            password: password
        }
        axios.post(`${API_URL}/user/connect`,data)
        .then((response) =>localStorage.setItem("account",response.data))
        .catch((error) =>console.error(error));
    };

    const handleInputNameChange = (e) => {
        setName(e.target.value);
    }; 
    const handleInputPasswordChange = (e) => {
        setPassword(e.target.value);
    };
    
    const handleSubmit = (e) => {
        e.preventDefault();
        connectAccount(name,password);
        window.location.href ='/account/info';
    }

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <label>
                    Nom :
                    <input
                        type="text"
                        name="Pseudo"
                        value={name}
                        onChange={handleInputNameChange}
                    />
                </label>
                <br/>
                <label>
                    Mot de Passe :
                    <input
                        type="text"
                        name="Mot de Passe"
                        value={password}
                        onChange={handleInputPasswordChange}
                    />
                </label>
                <br />
                <button type="submit">Se connecter</button>
            </form>
            <br/>
            <Link to="/account/new">Pas de Compte? Créer un Compte</Link>
        </div>
    );
}
export default ConnexionAuCompte;