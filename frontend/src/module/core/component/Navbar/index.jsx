import React from "react";
import "./style.scss";
import { useNavigate, useLocation, Link  } from "react-router-dom";
import { useState, useEffect } from 'react'

export default function Header() {
    const location = useLocation();
    const [path, setPath] = useState("");
    const navigate = useNavigate();
    const backToHome = () => {
        navigate("/");
    };

    useEffect(( ) => {
        setPath(location.pathname)
    }, [location])
    return (
        <>
            <header className="Navbar">
            <div className="container">
                <div className="app-name" onClick={backToHome}>
                    <img src="/logo.png" className="me-2 logo"/>
                    Game Theory & Matching Theory Solver
                </div>
                <div className="nav-item-container">
                    <Link to='/' className={path == "/" ? "nav-item highlight" : 'nav-item'}>Game Theory</Link> 
                    <Link to='/matching-theory/input' className={path == "/matching-theory/input" ? "nav-item highlight" : 'nav-item'}>Matching Problem</Link>
                    <Link to='/guide' className={path == "/guide" ? "nav-item highlight" : 'nav-item'}>Guide</Link>
                    <div className={path == "/about" ? "nav-item highlight" : 'nav-item'}>About</div>
                </div>
            </div>


            </header>
            <a className="app-info" href="http://moeaframework.org/" target="_blank">
                <img className='moea-framework-logo' src="https://th.bing.com/th/id/R.813fc73036588da433a26877f9738f63?rik=QW%2fZfuTteoFJug&riu=http%3a%2f%2fmoeaframework.org%2fimages%2flogo2_small.png&ehk=Yj9EPtTmS%2brN1WGUO7EUHyKgEVdiu9Drs0y9NoLKJXM%3d&risl=&pid=ImgRaw&r=0" alt="" />
                <p>Powered by MOEA Framework</p>
            </a>
        </>

    );
}
